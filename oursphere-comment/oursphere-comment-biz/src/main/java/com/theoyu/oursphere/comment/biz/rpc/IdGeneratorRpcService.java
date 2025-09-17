package com.theoyu.oursphere.comment.biz.rpc;

import com.theoyu.oursphere.id.generator.api.IdGeneratorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class IdGeneratorRpcService {
    @Resource
    private IdGeneratorFeignApi idGeneratorFeignApi;
    /**
     * 生成评论 ID
     *
     * @return
     */
    public String generateCommentId() {
        return idGeneratorFeignApi.getSegmentId("leaf-segment-comment-id");
    }
}
