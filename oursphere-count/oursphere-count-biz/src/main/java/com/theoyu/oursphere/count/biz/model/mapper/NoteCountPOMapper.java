package com.theoyu.oursphere.count.biz.model.mapper;

import com.theoyu.oursphere.count.biz.model.entity.NoteCountPO;

public interface NoteCountPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteCountPO record);

    int insertSelective(NoteCountPO record);

    NoteCountPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteCountPO record);

    int updateByPrimaryKey(NoteCountPO record);
}