package com.theoyu.oursphere.auth.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.theoyu.framework.common.exception.BusinessException;
import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.auth.constants.RedisKeyConstants;
import com.theoyu.oursphere.auth.enums.ResponseCodeEnum;
import com.theoyu.oursphere.auth.model.vo.verificationcode.SendVerificationCodeReqVO;
import com.theoyu.oursphere.auth.service.VerificationCodeService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @Override
    public Response<?> sendVerificationCode(SendVerificationCodeReqVO reqVO) {
        // 获取手机号和构造 Redis Key
        String phone = reqVO.getPhone();
        String redisKey = RedisKeyConstants.buildVerificationCodeKey(phone); // 生成验证码

        boolean isSend = redisTemplate.hasKey(redisKey);

        if (isSend) {
            throw new BusinessException(ResponseCodeEnum.VERIFICATION_CODE_SEND_FREQUENTLY);
        }
        String verificationCode = RandomUtil.randomString(6);
        //TODO:调用第三方短信服务发送验证码

        log.info("==> 手机号: {}, 发送验证码：【{}】", phone, verificationCode);

        // 将验证码存入 Redis，设置过期时间为 5 分钟
        redisTemplate.opsForValue().set(redisKey, verificationCode,5, TimeUnit.MINUTES);
        return Response.success();
    }
}
