package com.theoyu.oursphere.user.biz.rpc;

import com.theoyu.oursphere.id.generator.api.IdGeneratorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class idGeneratorRpcService {
    @Resource
    private IdGeneratorFeignApi idGeneratorFeignApi;

    /**
     * Leaf 号段模式：用户应用 ID 业务标识
     */
    private static final String BIZ_TAG_USER_APP_ID = "leaf-segment-user-app-id";
    /**
     * Leaf 号段模式：用户 ID 业务标识
     */
    private static final String BIZ_TAG_USER_ID = "leaf-segment-user-id";
    /**
     * 调用分布式 ID 生成服务生成用户应用 ID
     *
     * @return
     */
    public String getUserAppId() {
        return idGeneratorFeignApi.getSegmentId(BIZ_TAG_USER_APP_ID);
    }
    /**
     * 调用分布式 ID 生成服务用户 ID
     *
     * @return
     */
    public String getUserId() {
        return idGeneratorFeignApi.getSegmentId(BIZ_TAG_USER_ID);
    }

}
