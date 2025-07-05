package com.theoyu.oursphere.auth.utils.sms;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import com.theoyu.framework.common.util.JsonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AliyunSmsHelper {

    @Resource
    private Client client;

    /**
     * 发送短信
     *
     * @param signName      短信签名
     * @param templateCode  短信模板Code
     * @param phone         手机号
     * @param templateParam 模板参数，JSON格式字符串
     */
    public void sendTextMessage(String signName, String templateCode, String phone, String templateParam) {
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setPhoneNumbers(phone)
                .setTemplateParam(templateParam);
        RuntimeOptions runtime = new RuntimeOptions();

        try {
            log.info("==> 开始短信发送, phone: {}, signName: {},templateParam: {}", phone, signName, templateParam);

            // 发送短信
            SendSmsResponse response = client.sendSmsWithOptions(sendSmsRequest, runtime);

            log.info("==> 短信服务响应结果: {}", JsonUtils.toJsonString(response));
        } catch (Exception error) {
            log.error("==> 短信发送错误: ", error);
        }
    }

}
