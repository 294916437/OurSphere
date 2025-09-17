package com.theoyu.oursphere.comment.biz.service;

import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.comment.biz.model.vo.PublishCommentReqVO;

public interface CommentService {

    /**
     * 发布评论
     * @param publishCommentReqVO
     * @return
     */
    Response<?> publishComment(PublishCommentReqVO publishCommentReqVO);
}