package com.theoyu.oursphere.search.service;

import com.theoyu.framework.common.response.PageResponse;
import com.theoyu.oursphere.search.model.vo.SearchUserReqVO;
import com.theoyu.oursphere.search.model.vo.SearchUserRspVO;

public interface  SearchService {
    /**
     * 搜索用户
     * @param searchUserReqVO
     * @return
     */
    PageResponse<SearchUserRspVO> searchUser(SearchUserReqVO searchUserReqVO);
}
