package com.theoyu.oursphere.count.biz.model.mapper;

import com.theoyu.oursphere.count.biz.model.entity.UserCountPO;

public interface UserCountPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserCountPO record);

    int insertSelective(UserCountPO record);

    UserCountPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserCountPO record);

    int updateByPrimaryKey(UserCountPO record);
}