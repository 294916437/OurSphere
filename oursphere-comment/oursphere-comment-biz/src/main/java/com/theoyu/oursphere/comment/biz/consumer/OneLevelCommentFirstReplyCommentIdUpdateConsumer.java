package com.theoyu.oursphere.comment.biz.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.theoyu.framework.common.utils.JsonUtils;
import com.theoyu.oursphere.comment.biz.constants.MQConstants;
import com.theoyu.oursphere.comment.biz.constants.RedisKeyConstants;
import com.theoyu.oursphere.comment.biz.enums.CommentLevelEnum;
import com.theoyu.oursphere.comment.biz.model.dto.CountPublishCommentMqDTO;
import com.theoyu.oursphere.comment.biz.model.entity.CommentPO;
import com.theoyu.oursphere.comment.biz.model.mapper.CommentPOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@RocketMQMessageListener(consumerGroup = "oursphere_group_first_reply_comment_id" + MQConstants.TOPIC_COUNT_NOTE_COMMENT, // Group 组
        topic = MQConstants.TOPIC_COUNT_NOTE_COMMENT // 主题 Topic
)
@Slf4j
public class OneLevelCommentFirstReplyCommentIdUpdateConsumer implements RocketMQListener<String> {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    private BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000) // 缓存队列的最大容量
            .batchSize(1000)   // 一批次最多聚合 1000 条
            .linger(Duration.ofSeconds(1)) // 多久聚合一次（1s 一次）
            .setConsumerEx(this::consumeMessage) // 设置消费者方法
            .build();
    @Resource
    private CommentPOMapper commentPOMapper;
    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public void onMessage(String body) {
        // 往 bufferTrigger 中添加元素
        bufferTrigger.enqueue(body);
    }

    private void consumeMessage(List<String> bodys) {
        log.info("==> 【一级评论 first_reply_comment_id 更新】聚合消息, {}", JsonUtils.toJsonString(bodys));

        // 将聚合后的消息体 Json 转 List<CountPublishCommentMqDTO>
        List<CountPublishCommentMqDTO> publishCommentMqDTOS = Lists.newArrayList();
        bodys.forEach(body -> {
            try {
                List<CountPublishCommentMqDTO> list = JsonUtils.parseList(body, CountPublishCommentMqDTO.class);
                publishCommentMqDTOS.addAll(list);
            } catch (Exception e) {
                log.error("", e);
            }
        });

        // 过滤出二级评论的 parent_id（即一级评论 ID），并去重，需要更新对应一级评论的 first_reply_comment_id
        List<Long> parentIds = publishCommentMqDTOS.stream()
                .filter(publishCommentMqDTO -> Objects.equals(publishCommentMqDTO.getLevel(), CommentLevelEnum.TWO.getCode()))
                .map(CountPublishCommentMqDTO::getParentId)
                .distinct() // 去重
                .toList();

        if (CollUtil.isEmpty(parentIds)) return;

        // 构建Redis Ket，首先查询缓存中一级评价的二级评论ID
        List<String> keys = parentIds.stream()
                .map(RedisKeyConstants::buildHaveFirstReplyCommentKey).toList();

        // 批量查询 Redis
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);

        // 批量查询 Redis 中不存在的评论ID
        List<Long> missingCommentIds = Lists.newArrayList();
        for(int i = 0; i < keys.size(); i++) {
            if (Objects.isNull(values.get(i))){
                missingCommentIds.add(parentIds.get(i));
            }
        }
        // 若一级评价ID存在， 则说明DB中对应记录的 first_reply_comment_id 已经有值
        if(CollUtil.isNotEmpty(missingCommentIds)){
            // 不存在的，要查询DB并更新对应的缓存

            List<CommentPO> commentPOS =commentPOMapper.selectByCommentIds(missingCommentIds);

            // 异步同步到缓存中
            threadPoolTaskExecutor.submit(() -> {
                List<Long> needSyncCommentIds = commentPOS.stream()
                        .filter(commentPO -> Objects.nonNull(commentPO.getFirstReplyCommentId()))
                        .map(CommentPO::getId)
                        .toList();
                sync2Redis(needSyncCommentIds);
            });

            // 更新 DB 中对应的一级评论（值为0）的 first_reply_comment_id

            List<CommentPO> needUpdateCommentPOS = commentPOS.stream()
                    .filter(commentPO -> Objects.isNull(commentPO.getFirstReplyCommentId()))
                    .toList();

            needUpdateCommentPOS.forEach(commentPO -> {
               // 一级评价ID
               Long needUpdateCommentId = commentPO.getId();

               CommentPO latestCommentPO = commentPOMapper.selectLatestByParentId(needUpdateCommentId);

               if(Objects.nonNull(latestCommentPO)){
                   // 更新 DB
                   Long latestCommentPOId = latestCommentPO.getId();

                   commentPOMapper.updateFirstReplyCommentIdByPrimaryKey(latestCommentPOId, needUpdateCommentId);

                   // 同步到Redis缓存
                   threadPoolTaskExecutor.submit(() -> {
                       sync2Redis(Lists.newArrayList(needUpdateCommentId));
                   });

               }

            });

        }
    }
    /**
     * 同步到 Redis 中
     *
     * @param needSyncCommentIds
     */
    private void sync2Redis(List<Long> needSyncCommentIds) {
        // 获取 ValueOperations
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();

        // 使用 RedisTemplate 的管道模式，允许在一个操作中批量发送多个命令，防止频繁操作 Redis
        redisTemplate.executePipelined((RedisCallback<?>) (connection) -> {
            needSyncCommentIds.forEach(needSyncCommentId -> {
                // 构建 Redis Key
                String key = RedisKeyConstants.buildHaveFirstReplyCommentKey(needSyncCommentId);

                // 批量设置值并指定过期时间（5小时以内）
                valueOperations.set(key, 1, RandomUtil.randomInt(5 * 60 * 60), TimeUnit.SECONDS);
            });
            return null;
        });
    }

}
