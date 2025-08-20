package com.theoyu.oursphere.user.biz.utils.generator;

import com.theoyu.oursphere.user.biz.utils.generator.SnowFlake;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 基于雪花算法的全局ID生成器
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IdGeneratorHelper {

    private final SnowFlake snowFlake;

    /**
     * 生成全局唯一ID
     */
    public long generateId() {
        try {
            return snowFlake.nextId();
        } catch (Exception e) {
            log.error("Failed to generate ID", e);
            throw new RuntimeException("ID generation failed", e);
        }
    }

    /**
     * 生成字符串类型ID
     */
    public String generateStringId() {
        return String.valueOf(generateId());
    }

    /**
     * 解析ID信息
     */
    public SnowFlake.IdInfo parseId(long id) {
        return snowFlake.parseId(id);
    }

    /**
     * 批量生成ID
     */
    public long[] generateIds(int count) {
        if (count <= 0 || count > 1000) {
            throw new IllegalArgumentException("Count must be between 1 and 1000");
        }

        long[] ids = new long[count];
        for (int i = 0; i < count; i++) {
            ids[i] = generateId();
        }
        return ids;
    }
}