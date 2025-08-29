package com.theoyu.oursphere.count.biz.model.mapper;

import com.theoyu.oursphere.count.biz.model.entity.NoteLikePO;

public interface NoteLikePOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteLikePO record);

    int insertSelective(NoteLikePO record);

    NoteLikePO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteLikePO record);

    int updateByPrimaryKey(NoteLikePO record);
}