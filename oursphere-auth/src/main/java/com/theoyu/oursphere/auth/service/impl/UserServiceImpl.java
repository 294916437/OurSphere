package com.theoyu.oursphere.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.google.common.base.Preconditions;
import com.theoyu.framework.common.enums.DeletedEnum;
import com.theoyu.framework.common.enums.StatusEnum;
import com.theoyu.framework.common.response.Response;
import com.theoyu.framework.common.utils.JsonUtils;
import com.theoyu.oursphere.auth.constants.RedisKeyConstants;
import com.theoyu.oursphere.auth.constants.RoleConstants;
import com.theoyu.oursphere.auth.enums.LoginTypeEnum;
import com.theoyu.oursphere.auth.enums.ResponseCodeEnum;
import com.theoyu.oursphere.auth.model.entity.RolePO;
import com.theoyu.oursphere.auth.model.entity.UserPO;
import com.theoyu.oursphere.auth.model.entity.UserRolePO;
import com.theoyu.oursphere.auth.model.mapper.RolePOMapper;
import com.theoyu.oursphere.auth.model.mapper.UserPOMapper;
import com.theoyu.oursphere.auth.model.mapper.UserRolePOMapper;
import com.theoyu.oursphere.auth.model.vo.user.UserLoginReqVO;
import com.theoyu.oursphere.auth.service.UserService;
import com.theoyu.oursphere.auth.utils.generator.IdGeneratorHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Resource
    private UserPOMapper userPOMapper;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private UserRolePOMapper userRolePOMapper;
    @Resource
    private RolePOMapper rolePOMapper;
    @Resource
    private IdGeneratorHelper idGeneratorHelper;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Override
    public Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO) {
        String phone = userLoginReqVO.getPhone();
        Integer type = userLoginReqVO.getType();
        LoginTypeEnum loginTypeEnum = LoginTypeEnum.valueOf(type);
        Long userId = null;

        if (loginTypeEnum == LoginTypeEnum.PHONE_CODE) {
            // 手机号+验证码登录
            String code = userLoginReqVO.getCode();
            // 校验入参验证码是否为空，抛出全局的IllegalArgumentException异常，可以在全局异常处理器中捕获并返回自定义的错误响应
            Preconditions.checkArgument(StringUtils.isNotBlank(code), "验证码不能为空");
            // 通过手机号查询记录
            UserPO userPO = userPOMapper.selectPwdByPhone(phone);
            log.info("==> 用户是否注册, phone: {}, userPO: {}", phone, JsonUtils.toJsonString(userPO));

            // 判断是否注册
            if (Objects.isNull(userPO)) {
                // TODO:系统自动注册该用户
                userId = registerUser(phone);
            } else {
                // 已注册，则获取其用户 ID
                userId = userPO.getId();
            }
        } else if (loginTypeEnum == LoginTypeEnum.ACCOUNT_PASSWORD) {
            // TODO:账号+密码登录

        }
        StpUtil.login(userId);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return Response.success(tokenInfo.tokenValue);
    }
    /**
     * 系统自动注册用户
     * @param phone
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Long registerUser(String phone) {
        return transactionTemplate.execute(status -> {
            try {
            // 获取全局唯一的ID
            String userAppId = idGeneratorHelper.generateStringId();

            UserPO userPO = UserPO.builder()
                    .phone(phone)
                    .userId(userAppId) // 自动生成的用户凭证，64bit
                    .nickname("新用户") // 自动生成昵称
                    .status(StatusEnum.ENABLE.getValue()) // 状态为启用
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .isDeleted(DeletedEnum.NO.getValue()) // 逻辑删除
                    .build();

            // 添加入库
            userPOMapper.insert(userPO);
            // 获取刚刚添加入库的用户 ID
            Long userId = userPO.getId();
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
            // 将该用户的角色存入 Redis 中
            List<String> roles = new ArrayList<>(1);
            roles.add(rolePO.getRoleKey());
            String userRolesKey = RedisKeyConstants.buildUserRoleKey(userId);
            redisTemplate.opsForValue().set(userRolesKey, JsonUtils.toJsonString(roles));

            return userId;
        } catch (Exception e) {
            log.error("用户注册失败, phone: {}, error: {}", phone, e.getMessage(), e);
            status.setRollbackOnly();
            return null;
        }
        });
    }
}
