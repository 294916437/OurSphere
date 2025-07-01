package com.theoyu.oursphere.auth;
import  com.theoyu.framework.common.util.JsonUtils;
import com.theoyu.oursphere.auth.domain.dataobject.UserDo;
import com.theoyu.oursphere.auth.domain.mapper.UserDoMapper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
@Slf4j
class OursphereAuthApplicationTests {

    @Resource
    private UserDoMapper userDoMapper;

    /**
     * 测试插入数据
     */
    @Test
    void testInsert() {
        UserDo userDo = UserDo.builder()
                .username("oursphere")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        userDoMapper.insert(userDo);
    }

    /**
     * 查询数据
     */
    @Test
    void testSelect() {
        // 查询主键 ID 为 4 的记录
        UserDo userDO = userDoMapper.selectByPrimaryKey(1L);
        log.info("User: {}", JsonUtils.toJsonString(userDO));
    }

    @Test
    void testUpdate() {
        UserDo userDO = UserDo.builder()
                .id(1L)
                .username("oursphere")
                .updateTime(LocalDateTime.now())
                .build();

        // 根据主键 ID 更新记录
        userDoMapper.updateByPrimaryKey(userDO);
    }

    @Test
    void testDelete() {
        // 删除主键 ID 为 4 的记录
        userDoMapper.deleteByPrimaryKey(1L);
    }

}
