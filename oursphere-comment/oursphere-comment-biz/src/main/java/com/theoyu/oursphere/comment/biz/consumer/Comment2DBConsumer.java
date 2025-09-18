package com.theoyu.oursphere.comment.biz.consumer;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.RateLimiter;
import com.theoyu.framework.common.utils.JsonUtils;
import com.theoyu.oursphere.comment.biz.constants.MQConstants;
import com.theoyu.oursphere.comment.biz.enums.CommentLevelEnum;
import com.theoyu.oursphere.comment.biz.model.bo.CommentBO;
import com.theoyu.oursphere.comment.biz.model.dto.CountPublishCommentMqDTO;
import com.theoyu.oursphere.comment.biz.model.dto.PublishCommentMqDTO;
import com.theoyu.oursphere.comment.biz.model.entity.CommentPO;
import com.theoyu.oursphere.comment.biz.model.mapper.CommentPOMapper;
import com.theoyu.oursphere.comment.biz.rpc.KeyValueRpcService;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Comment2DBConsumer {
    @Value("${rocketmq.name-server}")
    private String namesrvAddr;
    @Resource
    private CommentPOMapper commentPOMapper;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private KeyValueRpcService keyValueRpcService;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    private DefaultMQPushConsumer consumer;

    // 每秒创建 1000 个令牌
    private RateLimiter rateLimiter = RateLimiter.create(1000);

    @Bean
    public DefaultMQPushConsumer mqPushConsumer() throws MQClientException {
        // Group 组
        String group = "oursphere_group_" + MQConstants.TOPIC_PUBLISH_COMMENT;

        // 创建一个新的 DefaultMQPushConsumer 实例，并指定消费者的消费组名
        consumer = new DefaultMQPushConsumer(group);

        // 设置 RocketMQ 的 NameServer 地址
        consumer.setNamesrvAddr(namesrvAddr);

        // 订阅指定的主题，并设置主题的订阅规则（"*" 表示订阅所有标签的消息）
        consumer.subscribe(MQConstants.TOPIC_PUBLISH_COMMENT, "*");

        // 设置消费者消费消息的起始位置，如果队列中没有消息，则从最新的消息开始消费。
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);

        // 设置消息消费模式，这里使用集群模式 (CLUSTERING)
        consumer.setMessageModel(MessageModel.CLUSTERING);

        // 设置每批次消费的最大消息数量，这里设置为 30，表示每次拉取时最多消费 30 条消息
        consumer.setConsumeMessageBatchMaxSize(30);

        // 注册消息监听器
        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            log.info("==> 本批次消息大小: {}", msgs.size());
            try {
                // 令牌桶流控
                rateLimiter.acquire();

                List<PublishCommentMqDTO> publishCommentMqDTOS= Lists.newArrayList();
                msgs.forEach(msg -> {
                    String msgJson = new String(msg.getBody());
                    log.info("==> Consumer - Received message: {}", msgJson);
                    publishCommentMqDTOS.add(JsonUtils.parseObject(msgJson, PublishCommentMqDTO.class));
                });

                // 提取所有不为空的回复评论 ID
                List<Long> replyCommentIds = publishCommentMqDTOS.stream()
                        .filter(publishCommentMqDTO -> Objects.nonNull(publishCommentMqDTO.getReplyCommentId()))
                        .map(PublishCommentMqDTO::getReplyCommentId).toList();

                // 批量查询相关回复评论记录
                List<CommentPO> replyCommentPOS = null;
                if (CollUtil.isNotEmpty(replyCommentIds)) {
                    // 查询数据库
                    replyCommentPOS = commentPOMapper.selectByCommentIds(replyCommentIds);
                }

                // DO 集合转 <评论 ID - 评论 DO> 字典, 以方便后续查找
                Map<Long, CommentPO> commentIdAndCommentDOMap = Maps.newHashMap();
                if (CollUtil.isNotEmpty(replyCommentPOS)) {
                    commentIdAndCommentDOMap = replyCommentPOS.stream().collect(Collectors.toMap(CommentPO::getId, commentDO -> commentDO));
                }

                // DTO转化为BO
                List<CommentBO> commentBOS = Lists.newArrayList();
                for(PublishCommentMqDTO publishCommentMqDTO : publishCommentMqDTOS) {
                    String imageUrl = publishCommentMqDTO.getImageUrl();
                    CommentBO commentBO = CommentBO.builder()
                            .id(publishCommentMqDTO.getCommentId())
                            .noteId(publishCommentMqDTO.getNoteId())
                            .userId(publishCommentMqDTO.getCreatorId())
                            .isContentEmpty(true) // 默认评论内容为空
                            .imageUrl(StringUtils.isBlank(imageUrl) ? "" : imageUrl)
                            .level(CommentLevelEnum.ONE.getCode()) // 默认为一级评论
                            .parentId(publishCommentMqDTO.getNoteId()) // 默认设置为所属笔记 ID
                            .createTime(publishCommentMqDTO.getCreateTime())
                            .updateTime(publishCommentMqDTO.getCreateTime())
                            .isTop(false)
                            .replyTotal(0L)
                            .likeTotal(0L)
                            .replyCommentId(0L)
                            .replyUserId(0L)
                            .build();

                    String content = publishCommentMqDTO.getContent();
                    if(StringUtils.isNotBlank(content)) {
                        commentBO.setContentUuid(UUID.randomUUID().toString());
                        commentBO.setContent(content);
                        commentBO.setIsContentEmpty(false); // 有评论内容，设置为 false
                    }


                    // 设置评论级别
                    Long replyCommentId = publishCommentMqDTO.getReplyCommentId();
                    if (Objects.nonNull(replyCommentId)) {
                        CommentPO replyCommentPO = commentIdAndCommentDOMap.get(replyCommentId);

                        if (Objects.nonNull(replyCommentPO)) {
                            // 若回复的评论 ID 不为空，说明是二级评论
                            commentBO.setLevel(CommentLevelEnum.TWO.getCode());

                            commentBO.setReplyCommentId(publishCommentMqDTO.getReplyCommentId());
                            // 父评论 ID
                            commentBO.setParentId(replyCommentPO.getId());
                            if (Objects.equals(replyCommentPO.getLevel(), CommentLevelEnum.TWO.getCode())) { // 如果回复的评论属于二级评论
                                commentBO.setParentId(replyCommentPO.getParentId());
                            }
                            // 回复的哪个用户
                            commentBO.setReplyUserId(replyCommentPO.getUserId());
                        }
                    }

                    commentBOS.add(commentBO);

                }

                log.info("## 筛选后的 CommentBOS: {}", JsonUtils.toJsonString(commentBOS));
                // 编程式事务，保证整体操作的原子性
                Integer insertedRows =  transactionTemplate.execute(status -> {
                    try {
                        // 先批量存入评论元数据
                        int count = commentPOMapper.batchInsert(commentBOS);

                        // 过滤出评论内容不为空的 BO
                        List<CommentBO> commentContentNotEmptyBOS = commentBOS.stream()
                                .filter(commentBO -> Boolean.FALSE.equals(commentBO.getIsContentEmpty()))
                                .toList();
                        if (CollUtil.isNotEmpty(commentContentNotEmptyBOS)) {
                            // KV服务批量存入评论内容
                            keyValueRpcService.batchSaveCommentContent(commentContentNotEmptyBOS);
                        }
                        // 获取插入成功的行数
                        return count;
                    } catch (Exception ex) {
                        status.setRollbackOnly(); // 标记事务为回滚
                        log.error("", ex);
                        throw ex;
                    }
                });
                if(Objects.nonNull(insertedRows)&& insertedRows > 0) {
                    // 构建发送给计数服务的 DTO 集合
                    List<CountPublishCommentMqDTO> countPublishCommentMqDTOS = publishCommentMqDTOS.stream()
                            .map(publishCommentMqDTO -> CountPublishCommentMqDTO.builder()
                                    .noteId(publishCommentMqDTO.getNoteId())
                                    .commentId(publishCommentMqDTO.getCommentId())
                                    .build())
                            .toList();

                    // 异步发送计数 MQ
                    org.springframework.messaging.Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(countPublishCommentMqDTOS))
                            .build();

                    // 异步发送 MQ 消息
                    rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_NOTE_COMMENT, message, new SendCallback() {
                        @Override
                        public void onSuccess(SendResult sendResult) {
                            log.info("==> 【计数: 评论发布】MQ 发送成功，SendResult: {}", sendResult);
                        }

                        @Override
                        public void onException(Throwable throwable) {
                            log.error("==> 【计数: 评论发布】MQ 发送异常: ", throwable);
                        }
                    });
                }
                // 手动 ACK，告诉 RocketMQ 这批次消息消费成功
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            } catch (Exception e) {
                log.error("", e);
                // 手动 ACK，告诉 RocketMQ 这批次消息处理失败，稍后再进行重试
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        });

        // 启动消费者
        consumer.start();
        return consumer;
    }

    @PreDestroy
    public void destroy() {
        if (Objects.nonNull(consumer)) {
            try {
                consumer.shutdown();  // 关闭消费者
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }

}

