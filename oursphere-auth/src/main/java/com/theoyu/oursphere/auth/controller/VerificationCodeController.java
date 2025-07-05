package com.theoyu.oursphere.auth.controller;

import com.theoyu.framework.common.response.Response;
import com.theoyu.framework.logger.aspect.ApiOperationLog;
import com.theoyu.oursphere.auth.model.vo.verificationcode.SendVerificationCodeReqVO;
import com.theoyu.oursphere.auth.service.VerificationCodeService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VerificationCodeController {

    @Resource
    private VerificationCodeService verificationCodeService;


    @PostMapping("/verification/code/send")
    @ApiOperationLog(description = "发送短信验证码")
    public Response<?> sendVerificationCode(@Validated @RequestBody SendVerificationCodeReqVO reqVO) {
        return verificationCodeService.sendVerificationCode(reqVO);
    }


}
