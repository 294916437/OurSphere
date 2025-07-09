package com.theoyu.oursphere.auth.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.theoyu.framework.common.exception.BusinessException;
import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.auth.constants.RedisKeyConstants;
import com.theoyu.oursphere.auth.enums.ResponseCodeEnum;
import com.theoyu.oursphere.auth.model.vo.verificationcode.SendVerificationCodeReqVO;
import com.theoyu.oursphere.auth.service.VerificationCodeService;
import com.theoyu.oursphere.auth.utils.sms.AliyunSmsHelper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private AliyunSmsHelper aliyunSmsHelper;


    @Override
    public Response<?> sendVerificationCode(SendVerificationCodeReqVO reqVO) {
        // 获取手机号和构造 Redis Key
        String phone = reqVO.getPhone();
        String redisKey = RedisKeyConstants.buildVerificationCodeKey(phone); // 生成验证码

        boolean isSend = redisTemplate.hasKey(redisKey);

        if (isSend) {
            throw new BusinessException(ResponseCodeEnum.VERIFICATION_CODE_SEND_FREQUENTLY);
        }
        String verificationCode = RandomUtil.randomNumbers(6);

        log.info("==> 手机号: {}, 即将发送的验证码：【{}】", phone, verificationCode);

        //在异步线程池中调用阿里云短信服务发送验证码
        //TODO:阿里云暂时无法申请短信签名，因此这个接口当前仅用于测试，实际使用时需要替换为有效的短信签名和模板代码
//        threadPoolTaskExecutor.submit(() -> {
//            String signName = "阿里云短信测试";
//            String templateCode = "SMS_322255324";
//            String templateParam = String.format("{\"code\":\"%s\"}", verificationCode);
//            aliyunSmsHelper.sendTextMessage(signName, templateCode, phone, templateParam);
//        });

        // 将验证码存入 Redis，设置过期时间为 5 分钟
        redisTemplate.opsForValue().set(redisKey, verificationCode,5, TimeUnit.MINUTES);
        return Response.success();
    }
}
