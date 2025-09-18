package com.theoyu.oursphere.comment.biz.service;

import com.theoyu.framework.common.response.PageResponse;
import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.comment.biz.model.vo.FindCommentItemRspVO;
import com.theoyu.oursphere.comment.biz.model.vo.FindCommentPageListReqVO;
import com.theoyu.oursphere.comment.biz.model.vo.PublishCommentReqVO;

public interface CommentService {

    /**
     * 发布评论
     * @param publishCommentReqVO
     * @return
     */
    Response<?> publishComment(PublishCommentReqVO publishCommentReqVO);
    /**
     * 评论列表分页查询
     * @param findCommentPageListReqVO
     * @return
     */
    PageResponse<FindCommentItemRspVO> findCommentPageList(FindCommentPageListReqVO findCommentPageListReqVO);

}