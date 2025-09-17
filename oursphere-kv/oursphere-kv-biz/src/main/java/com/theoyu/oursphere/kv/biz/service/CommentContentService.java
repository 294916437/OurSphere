package com.theoyu.oursphere.kv.biz.service;

import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.kv.dto.request.BatchAddCommentContentReqDTO;

public interface CommentContentService {


    /**
     * 批量添加评论内容
     * @param batchAddCommentContentReqDTO
     * @return
     */
    Response<?> batchAddCommentContent(BatchAddCommentContentReqDTO batchAddCommentContentReqDTO);
}