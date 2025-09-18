package com.theoyu.oursphere.comment.biz.consumer;

import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.theoyu.framework.common.utils.JsonUtils;
import com.theoyu.oursphere.comment.biz.constants.MQConstants;
import com.theoyu.oursphere.comment.biz.model.bo.CommentHeatBO;
import com.theoyu.oursphere.comment.biz.model.entity.CommentPO;
import com.theoyu.oursphere.comment.biz.model.mapper.CommentPOMapper;
import com.theoyu.oursphere.comment.biz.utils.HeatCalculator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@Component
@RocketMQMessageListener(consumerGroup = "xiaohashu_group_" + MQConstants.TOPIC_COMMENT_HEAT_UPDATE, // Group 组
        topic = MQConstants.TOPIC_COMMENT_HEAT_UPDATE // 主题 Topic
)
@Slf4j
public class CommentHeatUpdateConsumer implements RocketMQListener<String> {

    @Resource
    private CommentPOMapper commentPOMapper;

    private BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000) // 缓存队列的最大容量
            .batchSize(300)   // 一批次最多聚合 300 条
            .linger(Duration.ofSeconds(2)) // 多久聚合一次（2s 一次）
            .setConsumerEx(this::consumeMessage) // 设置消费者方法
            .build();

    @Override
    public void onMessage(String body) {
        // 往 bufferTrigger 中添加元素
        bufferTrigger.enqueue(body);
    }

    private void consumeMessage(List<String> bodys) {
        log.info("==> 【评论热度值计算】聚合消息, size: {}", bodys.size());
        log.info("==> 【评论热度值计算】聚合消息, {}", JsonUtils.toJsonString(bodys));

        // 将聚合后的消息体 Json 转 Set<Long>, 去重相同的评论 ID, 防止重复计算
        Set<Long> commentIds = Sets.newHashSet();
        bodys.forEach(body -> {
            try {
                Set<Long> list = JsonUtils.parseSet(body, Long.class);
                commentIds.addAll(list);
            } catch (Exception e) {
                log.error("", e);
            }
        });

        log.info("==> 去重后的评论 ID: {}", commentIds);

        // 执行评论热度值计算
        // 批量查询评论信息
        List<CommentPO> commentPOS = commentPOMapper.selectByCommentIds(commentIds.stream().toList());
        // 评论 ID
        List<Long> ids = Lists.newArrayList();
        // 热度值 BO
        List<CommentHeatBO> commentBOS = Lists.newArrayList();
        commentPOS.forEach(commentPO -> {
            Long commentId = commentPO.getId();
            Long likeTotal = commentPO.getLikeTotal();
            Long childCommentTotal = commentPO.getChildCommentTotal();

            BigDecimal heat = HeatCalculator.calculateHeat(likeTotal, childCommentTotal);
            ids.add(commentId);
            commentBOS.add(CommentHeatBO.builder()
                    .id(commentId)
                    .heat(heat.doubleValue())
                    .build());
        });

        // 评论更新评论的热度值
        commentPOMapper.batchUpdateHeatByCommentIds(ids,commentBOS);

    }
}