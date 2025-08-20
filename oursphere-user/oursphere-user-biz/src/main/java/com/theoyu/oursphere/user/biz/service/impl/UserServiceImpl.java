package com.theoyu.oursphere.user.biz.service.impl;

import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import com.theoyu.framework.common.constants.GlobalConstants;
import com.theoyu.framework.common.exception.BusinessException;
import com.theoyu.framework.common.response.Response;
import com.theoyu.framework.common.utils.ParamUtils;
import com.theoyu.framework.context.holder.LoginUserContextHolder;
import com.theoyu.oursphere.oss.api.FileFeignApi;
import com.theoyu.oursphere.user.biz.enums.ResponseCodeEnum;
import com.theoyu.oursphere.user.biz.enums.SexEnum;
import com.theoyu.oursphere.user.biz.model.entity.UserPO;
import com.theoyu.oursphere.user.biz.model.mapper.UserPOMapper;
import com.theoyu.oursphere.user.biz.model.vo.UpdateUserInfoReqVO;
import com.theoyu.oursphere.user.biz.rpc.OssRpcService;
import com.theoyu.oursphere.user.biz.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Resource
    private UserPOMapper userPOMapper;
    @Resource
    private OssRpcService ossRpcService;

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
            if(StringUtils.isBlank(avatarUrl)) {
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
            if(StringUtils.isBlank(backgroundImgUrl)) {
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
}
