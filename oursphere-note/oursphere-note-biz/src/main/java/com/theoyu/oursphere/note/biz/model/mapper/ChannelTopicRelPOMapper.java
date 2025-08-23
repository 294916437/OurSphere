package com.theoyu.oursphere.note.biz.model.mapper;

import com.theoyu.oursphere.note.biz.model.entity.ChannelTopicRelPO;

public interface ChannelTopicRelPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ChannelTopicRelPO record);

    int insertSelective(ChannelTopicRelPO record);

    ChannelTopicRelPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ChannelTopicRelPO record);

    int updateByPrimaryKey(ChannelTopicRelPO record);
}