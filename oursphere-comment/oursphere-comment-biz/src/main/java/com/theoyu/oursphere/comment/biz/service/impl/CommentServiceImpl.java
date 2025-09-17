package com.theoyu.oursphere.comment.biz.service.impl;

import com.google.common.base.Preconditions;
import com.theoyu.framework.common.response.Response;
import com.theoyu.framework.common.utils.JsonUtils;
import com.theoyu.framework.context.holder.LoginUserContextHolder;
import com.theoyu.oursphere.comment.biz.constants.MQConstants;
import com.theoyu.oursphere.comment.biz.model.dto.PublishCommentMqDTO;
import com.theoyu.oursphere.comment.biz.model.vo.PublishCommentReqVO;
import com.theoyu.oursphere.comment.biz.retry.SendMqRetryHelper;
import com.theoyu.oursphere.comment.biz.service.CommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private SendMqRetryHelper sendMqRetryHelper;
    /**
     * 发布评论
     *
     * @param publishCommentReqVO
     * @return
     */
    @Override
    public Response<?> publishComment(PublishCommentReqVO publishCommentReqVO) {
        // 评论正文
        String content = publishCommentReqVO.getContent();
        // 附近图片
        String imageUrl = publishCommentReqVO.getImageUrl();

        // 评论内容和图片不能同时为空
        Preconditions.checkArgument(StringUtils.isNotBlank(content) || StringUtils.isNotBlank(imageUrl),
                "评论正文和图片不能同时为空");

        // 发布者ID
        Long creatorId = LoginUserContextHolder.getUserId();

        PublishCommentMqDTO publishCommentMqDTO = PublishCommentMqDTO.builder()
                .noteId(publishCommentReqVO.getNoteId())
                .content(content)
                .imageUrl(imageUrl)
                .replyCommentId(publishCommentReqVO.getReplyCommentId())
                .createTime(LocalDateTime.now())
                .creatorId(creatorId)
                .build();
        // 使用重启工具发送MQ
        sendMqRetryHelper.asyncSend(MQConstants.TOPIC_PUBLISH_COMMENT, JsonUtils.toJsonString(publishCommentMqDTO));

        return Response.success();

    }
}
