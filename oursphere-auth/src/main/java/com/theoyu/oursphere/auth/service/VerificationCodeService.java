package com.theoyu.oursphere.auth.service;

import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.auth.model.vo.verificationcode.SendVerificationCodeReqVO;


public interface VerificationCodeService {
    /**
     * 发送验证码
     *
     * @param reqVO 请求参数
     * @return 响应结果
     */
    Response<?> sendVerificationCode(SendVerificationCodeReqVO reqVO);
}
