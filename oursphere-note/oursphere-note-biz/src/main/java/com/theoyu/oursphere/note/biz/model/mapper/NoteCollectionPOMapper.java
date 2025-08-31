package com.theoyu.oursphere.note.biz.model.mapper;

import com.theoyu.oursphere.note.biz.model.entity.NoteCollectionPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface NoteCollectionPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteCollectionPO record);

    int insertSelective(NoteCollectionPO record);

    NoteCollectionPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteCollectionPO record);

    int updateByPrimaryKey(NoteCollectionPO record);

    int selectCountByUserIdAndNoteId(@Param("userId") Long userId, @Param("noteId") Long noteId);

    List<NoteCollectionPO> selectByUserId(Long userId);

    int selectNoteIsCollected(@Param("userId") Long userId, @Param("noteId") Long noteId);

    List<NoteCollectionPO> selectCollectedByUserIdAndLimit(@Param("userId") Long userId, @Param("limit")  int limit);

    int insertOrUpdate(NoteCollectionPO noteCollectionPO);

}