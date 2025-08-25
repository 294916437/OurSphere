package com.theoyu.oursphere.note.biz.service;

import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.note.biz.model.vo.PublishNoteReqVO;

public interface  NoteService {
    /**
     * 笔记发布
     * @param publishNoteReqVO
     * @return
     */
    Response<?> publishNote(PublishNoteReqVO publishNoteReqVO);
}
