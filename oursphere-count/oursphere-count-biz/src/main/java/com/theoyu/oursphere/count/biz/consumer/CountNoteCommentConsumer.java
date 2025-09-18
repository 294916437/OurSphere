package com.theoyu.oursphere.count.biz.consumer;

import cn.hutool.core.collection.CollUtil;
import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.theoyu.framework.common.utils.JsonUtils;
import com.theoyu.oursphere.count.biz.constants.MQConstants;
import com.theoyu.oursphere.count.biz.model.dto.CountPublishCommentMqDTO;
import com.theoyu.oursphere.count.biz.model.mapper.NoteCountPOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RocketMQMessageListener(consumerGroup = "oursphere_group_" + MQConstants.TOPIC_COUNT_NOTE_COMMENT, // Group 组
        topic = MQConstants.TOPIC_COUNT_NOTE_COMMENT // 主题 Topic
)
@Slf4j
public class CountNoteCommentConsumer implements RocketMQListener<String> {

    @Resource
    private NoteCountPOMapper noteCountPOMapper;

    private BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000) // 缓存队列的最大容量
            .batchSize(1000)   // 一批次最多聚合 1000 条
            .linger(Duration.ofSeconds(1)) // 多久聚合一次（1s 一次）
            .setConsumerEx(this::consumeMessage) // 设置消费者方法
            .build();

    @Override
    public void onMessage(String body) {
        // 往 bufferTrigger 中添加元素
        bufferTrigger.enqueue(body);
    }

    private void consumeMessage(List<String> bodys) {
        log.info("==> 【笔记评论数】聚合消息, size: {}", bodys.size());
        log.info("==> 【笔记评论数】聚合消息, {}", JsonUtils.toJsonString(bodys));

        List<CountPublishCommentMqDTO> countPublishCommentMqDTOList = Lists.newArrayList();
        // 将消息体转换为List<CountPublishCommentMqDTO>
        bodys.forEach(body -> {
            try {
                List<CountPublishCommentMqDTO> list = JsonUtils.parseList(body, CountPublishCommentMqDTO.class);
                countPublishCommentMqDTOList.addAll(list);
            } catch (Exception e) {
                log.error("", e);
            }
        });

        // 根据ID进行分组
        Map<Long,List<CountPublishCommentMqDTO>> groupMap = countPublishCommentMqDTOList.stream()
                .collect(Collectors.groupingBy(CountPublishCommentMqDTO::getNoteId));

        // 更新笔记的评论总数
        for(Map.Entry<Long,List<CountPublishCommentMqDTO>> entry : groupMap.entrySet()) {
            // 笔记ID
            Long noteId = entry.getKey();
            // 指定笔记ID的评论数
            int count = CollUtil.size(entry.getValue());
            // 若评论数大于零，则执行更新操作：累加评论总数
            if (count > 0) {
                noteCountPOMapper.insertOrUpdateCommentTotalByNoteId(count, noteId);
            }
        }
    }
}

