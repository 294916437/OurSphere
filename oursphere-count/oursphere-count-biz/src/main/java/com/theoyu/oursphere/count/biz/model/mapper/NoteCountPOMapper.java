package com.theoyu.oursphere.count.biz.model.mapper;

import com.theoyu.oursphere.count.biz.model.entity.NoteCountPO;
import org.apache.ibatis.annotations.Param;

public interface NoteCountPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteCountPO record);

    int insertSelective(NoteCountPO record);

    NoteCountPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteCountPO record);

    int updateByPrimaryKey(NoteCountPO record);

    int insertOrUpdateLikeTotalByNoteId(@Param("count") Integer count, @Param("noteId") Long noteId);

    int insertOrUpdateCollectTotalByNoteId(@Param("count") Integer count, @Param("noteId") Long noteId);

    int insertOrUpdateLikeTotalByUserId(@Param("count") Integer count, @Param("userId") Long userId);



}