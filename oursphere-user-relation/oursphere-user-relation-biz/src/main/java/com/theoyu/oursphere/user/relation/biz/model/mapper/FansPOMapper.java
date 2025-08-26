package com.theoyu.oursphere.user.relation.biz.model.mapper;

import com.theoyu.oursphere.user.relation.biz.model.entity.FansPO;

public interface FansPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FansPO record);

    int insertSelective(FansPO record);

    FansPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FansPO record);

    int updateByPrimaryKey(FansPO record);
}