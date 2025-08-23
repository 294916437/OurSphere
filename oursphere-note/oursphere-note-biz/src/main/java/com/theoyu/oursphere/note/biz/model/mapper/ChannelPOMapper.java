package com.theoyu.oursphere.note.biz.model.mapper;

import com.theoyu.oursphere.note.biz.model.entity.ChannelPO;

public interface ChannelPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ChannelPO record);

    int insertSelective(ChannelPO record);

    ChannelPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ChannelPO record);

    int updateByPrimaryKey(ChannelPO record);
}