package com.theoyu.oursphere.search.controller;

import com.theoyu.framework.common.response.PageResponse;
import com.theoyu.framework.logger.aspect.ApiOperationLog;
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
}
