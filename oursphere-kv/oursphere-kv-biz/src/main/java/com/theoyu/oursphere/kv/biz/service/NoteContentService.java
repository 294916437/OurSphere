package com.theoyu.oursphere.kv.biz.service;

import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.kv.dto.request.AddNoteContentReqDTO;

public interface  NoteContentService {
    /**
     * 添加笔记内容
     *
     * @param addNoteContentReqDTO
     * @return
     */
    Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO);

}
