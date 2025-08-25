package com.theoyu.oursphere.kv.biz.service;

import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.kv.dto.request.AddNoteContentReqDTO;
import com.theoyu.oursphere.kv.dto.request.DeleteNoteContentReqDTO;
import com.theoyu.oursphere.kv.dto.request.FindNoteContentReqDTO;
import com.theoyu.oursphere.kv.dto.response.FindNoteContentRspDTO;

public interface  NoteContentService {
    /**
     * 添加笔记内容
     *
     * @param addNoteContentReqDTO
     * @return
     */
    Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO);
    /**
     * 查询笔记内容
     *
     * @param findNoteContentReqDTO
     * @return
     */
    Response<FindNoteContentRspDTO> findNoteContent(FindNoteContentReqDTO findNoteContentReqDTO);
    /**
     * 删除笔记内容
     *
     * @param deleteNoteContentReqDTO
     * @return
     */
    Response<?> deleteNoteContent(DeleteNoteContentReqDTO deleteNoteContentReqDTO);


}
