package com.theoyu.oursphere.note.biz.model.mapper;

import com.theoyu.oursphere.note.biz.model.entity.TopicPO;

public interface TopicPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TopicPO record);

    int insertSelective(TopicPO record);

    TopicPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TopicPO record);

    int updateByPrimaryKey(TopicPO record);
}