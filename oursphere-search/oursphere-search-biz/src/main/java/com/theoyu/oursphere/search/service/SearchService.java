package com.theoyu.oursphere.search.service;

import com.theoyu.framework.common.response.PageResponse;
import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.search.dto.RebuildNoteDocumentReqDTO;
import com.theoyu.oursphere.search.dto.RebuildUserDocumentReqDTO;
import com.theoyu.oursphere.search.model.vo.SearchNoteReqVO;
import com.theoyu.oursphere.search.model.vo.SearchNoteRspVO;
import com.theoyu.oursphere.search.model.vo.SearchUserReqVO;
import com.theoyu.oursphere.search.model.vo.SearchUserRspVO;

public interface  SearchService {
    /**
     * 搜索用户
     * @param searchUserReqVO
     * @return
     */
    PageResponse<SearchUserRspVO> searchUser(SearchUserReqVO searchUserReqVO);
    /**
     * 搜索笔记
     * @param searchNoteReqVO
     * @return
     */
    PageResponse<SearchNoteRspVO> searchNote(SearchNoteReqVO searchNoteReqVO);
    /**
     * 重建笔记文档
     * @param rebuildNoteDocumentReqDTO
     * @return
     */
    Response<Long> rebuildDocument(RebuildNoteDocumentReqDTO rebuildNoteDocumentReqDTO);
    /**
     * 重建用户文档
     * @param rebuildUserDocumentReqDTO
     * @return
     */
    Response<Long> rebuildDocument(RebuildUserDocumentReqDTO rebuildUserDocumentReqDTO);

}
