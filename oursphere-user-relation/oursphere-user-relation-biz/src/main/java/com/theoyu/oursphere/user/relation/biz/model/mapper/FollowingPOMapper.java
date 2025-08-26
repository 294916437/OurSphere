package com.theoyu.oursphere.user.relation.biz.model.mapper;

import com.theoyu.oursphere.user.relation.biz.model.entity.FollowingPO;

import java.util.List;

public interface FollowingPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FollowingPO record);

    int insertSelective(FollowingPO record);

    FollowingPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FollowingPO record);

    int updateByPrimaryKey(FollowingPO record);

    List<FollowingPO> selectByUserId(Long userId);
}