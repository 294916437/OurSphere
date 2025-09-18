package com.theoyu.oursphere.kv.biz.service;

import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.kv.dto.request.BatchAddCommentContentReqDTO;
import com.theoyu.oursphere.kv.dto.request.BatchFindCommentContentReqDTO;

public interface CommentContentService {


    /**
     * 批量添加评论内容
     * @param batchAddCommentContentReqDTO
     * @return
     */
    Response<?> batchAddCommentContent(BatchAddCommentContentReqDTO batchAddCommentContentReqDTO);
    /**
     * 批量查询评论内容
     * @param batchFindCommentContentReqDTO
     * @return
     */
    Response<?> batchFindCommentContent(BatchFindCommentContentReqDTO batchFindCommentContentReqDTO);

}