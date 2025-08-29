package com.theoyu.oursphere.count.biz.model.mapper;

import com.theoyu.oursphere.count.biz.model.entity.NoteCollectionPO;

public interface NoteCollectionPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteCollectionPO record);

    int insertSelective(NoteCollectionPO record);

    NoteCollectionPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteCollectionPO record);

    int updateByPrimaryKey(NoteCollectionPO record);
}