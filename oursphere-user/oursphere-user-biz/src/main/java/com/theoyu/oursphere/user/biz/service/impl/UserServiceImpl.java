package com.theoyu.oursphere.user.biz.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import com.theoyu.framework.common.enums.DeletedEnum;
import com.theoyu.framework.common.enums.StatusEnum;
import com.theoyu.framework.common.exception.BusinessException;
import com.theoyu.framework.common.response.Response;
import com.theoyu.framework.common.utils.JsonUtils;
import com.theoyu.framework.common.utils.ParamUtils;
import com.theoyu.framework.context.holder.LoginUserContextHolder;
import com.theoyu.oursphere.user.biz.constants.RedisKeyConstants;
import com.theoyu.oursphere.user.biz.constants.RoleConstants;
import com.theoyu.oursphere.user.biz.enums.ResponseCodeEnum;
import com.theoyu.oursphere.user.biz.enums.SexEnum;
import com.theoyu.oursphere.user.biz.model.entity.RolePO;
import com.theoyu.oursphere.user.biz.model.entity.UserPO;
import com.theoyu.oursphere.user.biz.model.entity.UserRolePO;
import com.theoyu.oursphere.user.biz.model.mapper.RolePOMapper;
import com.theoyu.oursphere.user.biz.model.mapper.UserPOMapper;
import com.theoyu.oursphere.user.biz.model.mapper.UserRolePOMapper;
import com.theoyu.oursphere.user.biz.model.vo.UpdateUserInfoReqVO;
import com.theoyu.oursphere.user.biz.rpc.OssRpcService;
import com.theoyu.oursphere.user.biz.rpc.idGeneratorRpcService;
import com.theoyu.oursphere.user.biz.service.UserService;
import com.theoyu.oursphere.user.dto.request.FindUserByIdReqDTO;
import com.theoyu.oursphere.user.dto.request.FindUserByPhoneReqDTO;
import com.theoyu.oursphere.user.dto.request.RegisterUserReqDTO;
import com.theoyu.oursphere.user.dto.request.UpdateUserPasswordReqDTO;
import com.theoyu.oursphere.user.dto.response.FindUserByIdRspDTO;
import com.theoyu.oursphere.user.dto.response.FindUserByPhoneRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Resource
    private idGeneratorRpcService idGeneratorRpcService;
    @Resource
    private UserPOMapper userPOMapper;
    @Resource
    private OssRpcService ossRpcService;
    @Resource
    private UserRolePOMapper userRolePOMapper;
    @Resource
    private RolePOMapper rolePOMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;


    @Override
    public Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO) {
        UserPO userPO = new UserPO();
        // 设置当前需要更新的用户 ID
        userPO.setId(LoginUserContextHolder.getUserId());
        // 标识位：是否需要更新
        boolean needUpdate = false;

        // 头像
        MultipartFile avatarFile = updateUserInfoReqVO.getAvatar();

        if (Objects.nonNull(avatarFile)) {
            String avatarUrl = ossRpcService.uploadFile(avatarFile);
            log.info("==> 上传头像成功，头像地址：{}", avatarUrl);
            if (StringUtils.isBlank(avatarUrl)) {
                throw new BusinessException(ResponseCodeEnum.UPLOAD_AVATAR_FAIL);
            }
            userPO.setAvatar(avatarUrl);
            needUpdate = true;
        }
        // 背景图
        MultipartFile backgroundImgFile = updateUserInfoReqVO.getBackgroundImg();
        if (Objects.nonNull(backgroundImgFile)) {
            String backgroundImgUrl = ossRpcService.uploadFile(backgroundImgFile);
            log.info("==> 上传背景图成功，背景图地址：{}", backgroundImgUrl);
            if (StringUtils.isBlank(backgroundImgUrl)) {
                throw new BusinessException(ResponseCodeEnum.UPLOAD_BACKGROUND_IMG_FAIL);
            }
            userPO.setBackgroundImg(backgroundImgUrl);
            needUpdate = true;
        }
        // 昵称
        String nickname = updateUserInfoReqVO.getNickname();
        if (StringUtils.isNotBlank(nickname)) {
            Preconditions.checkArgument(ParamUtils.checkNickname(nickname), ResponseCodeEnum.NICK_NAME_VALID_FAIL.getErrorMessage());
            userPO.setNickname(nickname);
            needUpdate = true;
        }

        // 用户应用ID
        String userAppId = updateUserInfoReqVO.getUserId();
        if (StringUtils.isNotBlank(userAppId)) {
            Preconditions.checkArgument(ParamUtils.checkUserAppId(userAppId), ResponseCodeEnum.USER_APP_ID_VALID_FAIL.getErrorMessage());
            userPO.setUserId(userAppId);
            needUpdate = true;
        }

        // 性别
        Integer sex = updateUserInfoReqVO.getSex();
        if (Objects.nonNull(sex)) {
            Preconditions.checkArgument(SexEnum.isValid(sex), ResponseCodeEnum.SEX_VALID_FAIL.getErrorMessage());
            userPO.setSex(sex);
            needUpdate = true;
        }

        // 生日
        LocalDate birthday = updateUserInfoReqVO.getBirthday();
        if (Objects.nonNull(birthday)) {
            userPO.setBirthday(birthday);
            needUpdate = true;
        }

        // 个人简介
        String introduction = updateUserInfoReqVO.getIntroduction();
        if (StringUtils.isNotBlank(introduction)) {
            Preconditions.checkArgument(ParamUtils.checkLength(introduction, 100), ResponseCodeEnum.INTRODUCTION_VALID_FAIL.getErrorMessage());
            userPO.setIntroduction(introduction);
            needUpdate = true;
        }


        if (needUpdate) {
            // 更新用户信息
            userPO.setUpdateTime(LocalDateTime.now());
            userPOMapper.updateByPrimaryKeySelective(userPO);
        }
        return Response.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> register(RegisterUserReqDTO registerUserReqDTO) {
        String phone = registerUserReqDTO.getPhone();

        // 先判断该手机号是否已被注册
        UserPO userPO1 = userPOMapper.selectByPhone(phone);

        log.info("==> 用户是否注册, phone: {}, userPO: {}", phone, JsonUtils.toJsonString(userPO1));

        // 若已注册，则直接返回用户 ID
        if (Objects.nonNull(userPO1)) {
            return Response.success(userPO1.getId());
        }

        // 否则注册新用户
        String userAppId = idGeneratorRpcService.getUserAppId();
        String userIdStr = idGeneratorRpcService.getUserId();
        Long userId = Long.valueOf(userIdStr);
        UserPO userPO = UserPO.builder()
                .id(userId)
                .phone(phone)
                .userId(userAppId) // 自动生成的用户凭证
                .nickname("新用户") // 自动生成昵称
                .status(StatusEnum.ENABLE.getValue()) // 状态为启用
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(DeletedEnum.NO.getValue()) // 逻辑删除
                .build();

        // 添加入库
        userPOMapper.insert(userPO);

        // 给该用户分配一个默认角色
        UserRolePO userRolePO = UserRolePO.builder()
                .userId(userId)
                .roleId(RoleConstants.COMMON_USER_ROLE_ID)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(DeletedEnum.NO.getValue())
                .build();
        userRolePOMapper.insert(userRolePO);

        RolePO rolePO = rolePOMapper.selectByPrimaryKey(RoleConstants.COMMON_USER_ROLE_ID);

        // 将该用户的角色 ID 存入 Redis 中
        List<String> roles = new ArrayList<>(1);
        roles.add(rolePO.getRoleKey());

        String userRolesKey = RedisKeyConstants.buildUserRoleKey(userId);
        redisTemplate.opsForValue().set(userRolesKey, JsonUtils.toJsonString(roles));

        return Response.success(userId);
    }

    @Override
    public Response<FindUserByPhoneRspDTO> findByPhone(FindUserByPhoneReqDTO findUserByPhoneReqDTO) {
        String phone = findUserByPhoneReqDTO.getPhone();

        // 根据手机号查询用户信息
        UserPO userPO = userPOMapper.selectByPhone(phone);

        // 判空
        if (Objects.isNull(userPO)) {
            throw new BusinessException(ResponseCodeEnum.USER_NOT_FOUND);
        }

        // 构建返参
        FindUserByPhoneRspDTO findUserByPhoneRspDTO = FindUserByPhoneRspDTO.builder()
                .id(userPO.getId())
                .password(userPO.getPassword())
                .build();

        return Response.success(findUserByPhoneRspDTO);

    }

    @Override
    public Response<?> updatePassword(UpdateUserPasswordReqDTO updateUserPasswordReqDTO) {
        // 获取当前请求对应的用户 ID
        Long userId = LoginUserContextHolder.getUserId();

        UserPO userPO = UserPO.builder()
                .id(userId)
                .password(updateUserPasswordReqDTO.getEncodePassword()) // 加密后的密码
                .updateTime(LocalDateTime.now())
                .build();
        // 更新密码
        userPOMapper.updateByPrimaryKeySelective(userPO);

        return Response.success();
    }

    @Override
    public Response<FindUserByIdRspDTO> findById(FindUserByIdReqDTO findUserByIdReqDTO) {
        Long userId = findUserByIdReqDTO.getId();

        // 用户缓存 Redis Key
        String userInfoRedisKey = RedisKeyConstants.buildUserInfoKey(userId);

        // 先从 Redis 缓存中查询
        String userInfoRedisValue = (String) redisTemplate.opsForValue().get(userInfoRedisKey);

        // 若 Redis 缓存中存在该用户信息
        if (StringUtils.isNotBlank(userInfoRedisValue)) {
            // 将存储的 Json 字符串转换成对象，并返回
            FindUserByIdRspDTO findUserByIdRspDTO = JsonUtils.parseObject(userInfoRedisValue, FindUserByIdRspDTO.class);
            return Response.success(findUserByIdRspDTO);
        }

        // 否则, 从数据库中查询用户信息
        UserPO userPO = userPOMapper.selectByPrimaryKey(userId);

        if (Objects.isNull(userPO)) {
            threadPoolTaskExecutor.execute(() -> {
                // 防止缓存穿透，将空数据存入 Redis 缓存 (过期时间不宜设置过长)
                // 保底1分钟 + 随机秒数
                long expireSeconds = 60 + RandomUtil.randomInt(60);
                redisTemplate.opsForValue().set(userInfoRedisKey, "null", expireSeconds, TimeUnit.SECONDS);
            });
            throw new BusinessException(ResponseCodeEnum.USER_NOT_FOUND);
        }

        // 构建返参
        FindUserByIdRspDTO findUserByIdRspDTO = FindUserByIdRspDTO.builder()
                .id(userPO.getId())
                .nickName(userPO.getNickname())
                .avatar(userPO.getAvatar())
                .build();

        // 异步将用户信息存入 Redis 缓存，提升响应速度
        threadPoolTaskExecutor.submit(() -> {
            // 过期时间（保底1天 + 随机秒数）
            long expireSeconds = 60*60*24 + RandomUtil.randomInt(60*60*24);
            redisTemplate.opsForValue()
                    .set(userInfoRedisKey, JsonUtils.toJsonString(findUserByIdRspDTO), expireSeconds, TimeUnit.SECONDS);
        });

        return Response.success(findUserByIdRspDTO);
    }

}
