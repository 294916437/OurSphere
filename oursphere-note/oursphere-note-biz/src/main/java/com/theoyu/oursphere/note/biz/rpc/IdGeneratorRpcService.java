package com.theoyu.oursphere.note.biz.rpc;

import com.theoyu.oursphere.id.generator.api.IdGeneratorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;


@Component
public class IdGeneratorRpcService {
    @Resource
    private IdGeneratorFeignApi idGeneratorFeignApi;
    /**
     * 生成雪花算法 ID
     *
     * @return
     */
    public String getSnowflakeId() {
        return idGeneratorFeignApi.getSnowflakeId("test");
    }

}
