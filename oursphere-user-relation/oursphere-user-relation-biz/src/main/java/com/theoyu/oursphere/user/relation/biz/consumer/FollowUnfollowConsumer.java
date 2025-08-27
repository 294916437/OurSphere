package com.theoyu.oursphere.user.relation.biz.consumer;


import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.theoyu.framework.common.utils.JsonUtils;
import com.theoyu.oursphere.user.relation.biz.constants.MQConstants;
import com.theoyu.oursphere.user.relation.biz.model.dto.FollowUserMqDTO;
import com.theoyu.oursphere.user.relation.biz.model.entity.FansPO;
import com.theoyu.oursphere.user.relation.biz.model.entity.FollowingPO;
import com.theoyu.oursphere.user.relation.biz.model.mapper.FansPOMapper;
import com.theoyu.oursphere.user.relation.biz.model.mapper.FollowingPOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 关注、取关 消费者
 */
@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "oursphere_group_"+MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW, // Group
        topic = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW // 消费的主题 Topic
)
public class FollowUnfollowConsumer implements RocketMQListener<Message> {

    @Resource
    private FollowingPOMapper followingPOMapper;
    @Resource
    private FansPOMapper fansPOMapper;
    @Resource
    private TransactionTemplate transactionTemplate;
    // 每秒创建 5000 个令牌
    @Resource
    private RateLimiter rateLimiter;

    @Override
    public void onMessage(Message message) {
        // 流量削峰：通过获取令牌，如果没有令牌可用，将阻塞，直到获得
        rateLimiter.acquire();
        // 消息体
        String bodyJsonStr = new String(message.getBody());
        // 标签
        String tags = message.getTags();

        log.info("==> FollowUnfollowConsumer 消费了消息 {}, tags: {}", bodyJsonStr, tags);

        // 根据 MQ 标签，判断操作类型
        if (Objects.equals(tags, MQConstants.TAG_FOLLOW)) { // 关注
            handleFollowTagMessage(bodyJsonStr);
        } else if (Objects.equals(tags, MQConstants.TAG_UNFOLLOW)) { // 取关
            // TOPO
        }
    }

    /**
     * 关注
     * @param bodyJsonStr
     */
    private void handleFollowTagMessage(String bodyJsonStr) {
        // 将消息体 json 字符串转为 DTO 对象
        FollowUserMqDTO followUserMqDTO = JsonUtils.parseObject(bodyJsonStr, FollowUserMqDTO.class);

        // 判空
        if (Objects.isNull(followUserMqDTO)) return;

        // 幂等性：通过联合唯一索引保证

        Long userId = followUserMqDTO.getUserId();
        Long followUserId = followUserMqDTO.getFollowUserId();
        LocalDateTime createTime = followUserMqDTO.getCreateTime();

        // 编程式提交事务
        boolean isSuccess = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                // 关注成功需往数据库添加两条记录
                // 关注表：一条记录
                int count = followingPOMapper.insert(FollowingPO.builder()
                        .userId(userId)
                        .followingUserId(followUserId)
                        .createTime(createTime)
                        .build());

                // 粉丝表：一条记录
                if (count > 0) {
                    fansPOMapper.insert(FansPO.builder()
                            .userId(followUserId)
                            .fansUserId(userId)
                            .createTime(createTime)
                            .build());
                }
                return true;
            } catch (Exception ex) {
                status.setRollbackOnly(); // 标记事务为回滚
                log.error("", ex);
            }
            return false;
        }));

        log.info("## 数据库添加记录结果：{}", isSuccess);
        // TODO: 更新 Redis 中被关注用户的 ZSet 粉丝列表
    }
}
