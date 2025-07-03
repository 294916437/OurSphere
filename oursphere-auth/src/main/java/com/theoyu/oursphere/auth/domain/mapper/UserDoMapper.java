package com.theoyu.oursphere.auth.domain.mapper;

import com.theoyu.oursphere.auth.domain.dataobject.UserDo;

public interface UserDoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserDo record);

    int insertSelective(UserDo record);

    UserDo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserDo record);

    int updateByPrimaryKey(UserDo record);
}