package com.theoyu.oursphere.search.controller;

import com.theoyu.framework.common.response.PageResponse;
import com.theoyu.framework.common.response.Response;
import com.theoyu.framework.logger.aspect.ApiOperationLog;
import com.theoyu.oursphere.search.dto.RebuildNoteDocumentReqDTO;
import com.theoyu.oursphere.search.dto.RebuildUserDocumentReqDTO;
import com.theoyu.oursphere.search.model.vo.SearchNoteReqVO;
import com.theoyu.oursphere.search.model.vo.SearchNoteRspVO;
import com.theoyu.oursphere.search.model.vo.SearchUserReqVO;
import com.theoyu.oursphere.search.model.vo.SearchUserRspVO;
import com.theoyu.oursphere.search.service.SearchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {
    @Resource
    private SearchService searchService;

    @PostMapping("/user")
    @ApiOperationLog(description = "搜索用户")
    public PageResponse<SearchUserRspVO> searchUser(@RequestBody @Validated SearchUserReqVO searchUserReqVO) {
        return searchService.searchUser(searchUserReqVO);

    }
    @PostMapping("/note")
    @ApiOperationLog(description = "搜索笔记")
    public PageResponse<SearchNoteRspVO> searchNote(@RequestBody @Validated SearchNoteReqVO searchNoteReqVO) {
        return searchService.searchNote(searchNoteReqVO);
    }
    // ===================================== 对其他服务提供的接口 =====================================
    @PostMapping("/note/document/rebuild")
    @ApiOperationLog(description = "用户文档重建")
    public Response<Long> rebuildDocument(@Validated @RequestBody RebuildNoteDocumentReqDTO rebuildNoteDocumentReqDTO) {
        return searchService.rebuildDocument(rebuildNoteDocumentReqDTO);
    }
    // ===================================== 对其他服务提供的接口 =====================================
    @PostMapping("/user/document/rebuild")
    @ApiOperationLog(description = "用户文档重建")
    public Response<Long> rebuildDocument(@Validated @RequestBody RebuildUserDocumentReqDTO rebuildUserDocumentReqDTO) {
        return searchService.rebuildDocument(rebuildUserDocumentReqDTO);
    }
}
