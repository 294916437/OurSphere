package com.theoyu.oursphere.user.relation.biz.service;

import com.theoyu.framework.common.response.Response;
import com.theoyu.oursphere.user.relation.biz.model.vo.FollowUserReqVO;
import com.theoyu.oursphere.user.relation.biz.model.vo.UnfollowUserReqVO;

public interface  RelationService {
    /**
     * 关注用户
     * @param followUserReqVO
     * @return
     */
    Response<?> follow(FollowUserReqVO followUserReqVO);
    /**
     * 取关用户
     * @param unfollowUserReqVO
     * @return
     */
    Response<?> unfollow(UnfollowUserReqVO unfollowUserReqVO);
}
