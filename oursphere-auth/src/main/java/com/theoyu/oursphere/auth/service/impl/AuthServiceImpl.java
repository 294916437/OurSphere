package com.theoyu.oursphere.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.google.common.base.Preconditions;
import com.theoyu.framework.common.exception.BusinessException;
import com.theoyu.framework.context.holder.LoginUserContextHolder;
import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.auth.constants.RedisKeyConstants;
import com.theoyu.oursphere.auth.enums.LoginTypeEnum;
import com.theoyu.oursphere.auth.enums.ResponseCodeEnum;
import com.theoyu.oursphere.auth.model.vo.user.UserLoginReqVO;
import com.theoyu.oursphere.auth.model.vo.user.UserPasswordReqVO;
import com.theoyu.oursphere.auth.rpc.UserRpcService;
import com.theoyu.oursphere.auth.service.AuthService;
import com.theoyu.oursphere.user.dto.response.FindUserByPhoneRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Resource
    private UserRpcService userRpcService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Override
    public Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO) {
        String phone = userLoginReqVO.getPhone();
        Integer type = userLoginReqVO.getType();
        LoginTypeEnum loginTypeEnum = LoginTypeEnum.valueOf(type);

        // 登录类型错误
        if (Objects.isNull(loginTypeEnum)) {
            throw new BusinessException(ResponseCodeEnum.LOGIN_TYPE_ERROR);
        }

        Long userId = null;

        if (loginTypeEnum == LoginTypeEnum.PHONE_CODE) {
            // 手机号+验证码登录
            String code = userLoginReqVO.getCode();
            // 校验入参验证码是否为空，抛出全局的IllegalArgumentException异常，可以在全局异常处理器中捕获并返回自定义的错误响应
            Preconditions.checkArgument(StringUtils.isNotBlank(code), "验证码不能为空");
            String key = RedisKeyConstants.buildVerificationCodeKey(phone);
            // 查询存储在 Redis 中该用户的登录验证码
            String sentCode = (String) redisTemplate.opsForValue().get(key);

            // 判断用户提交的验证码，与 Redis 中的验证码是否一致
            if (!StringUtils.equals(code, sentCode)) {
                throw new BusinessException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
            }

            Long userIdTmp = userRpcService.registerUser(phone);
            if(Objects.isNull(userIdTmp)) {
                throw new BusinessException(ResponseCodeEnum.LOGIN_FAIL);
            }
            userId = userIdTmp;

        } else if (loginTypeEnum == LoginTypeEnum.ACCOUNT_PASSWORD) {
            // 账号+密码登录
            String password = userLoginReqVO.getPassword();
            // 根据手机号查询
            FindUserByPhoneRspDTO findUserByPhoneRspDTO = userRpcService.findUserByPhone(phone);

            // 判断该手机号是否注册
            if (Objects.isNull(findUserByPhoneRspDTO)) {
                throw new BusinessException(ResponseCodeEnum.USER_NOT_FOUND);
            }

            // 拿到密文密码
            String encodePassword = findUserByPhoneRspDTO.getPassword();

            // 匹配密码是否一致
            boolean isPasswordCorrect = passwordEncoder.matches(password, encodePassword);

            // 如果不正确，则抛出业务异常，提示用户名或者密码不正确
            if (!isPasswordCorrect) {
                throw new BusinessException(ResponseCodeEnum.PHONE_OR_PASSWORD_ERROR);
            }

            userId = findUserByPhoneRspDTO.getId();

        }
        StpUtil.login(userId);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return Response.success(tokenInfo.tokenValue);
    }

    /**
     * 退出登录
     *
     * @return
     */
    @Override
    public Response<?> logout() {
        Long userId = LoginUserContextHolder.getUserId();

        log.info("==> 用户退出登录, userId: {}", userId);

        // 退出登录 (指定用户 ID)
        StpUtil.logout(userId);
        return Response.success();
    }

    @Override
    public Response<?> updatePassword(UserPasswordReqVO updatePasswordReqVO) {
        // 新密码
        String newPassword = updatePasswordReqVO.getNewPassword();
        // 密码加密
        String encodePassword = passwordEncoder.encode(newPassword);

        // RPC: 调用用户服务：更新密码
        userRpcService.updatePassword(encodePassword);

        return Response.success();
    }

}
