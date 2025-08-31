package com.theoyu.oursphere.note.biz.model.mapper;

import com.theoyu.oursphere.note.biz.model.entity.NoteLikePO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NoteLikePOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteLikePO record);

    int insertSelective(NoteLikePO record);

    NoteLikePO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteLikePO record);

    int updateByPrimaryKey(NoteLikePO record);

    int selectCountByUserIdAndNoteId(@Param("userId") Long userId, @Param("noteId") Long noteId);

    List<NoteLikePO> selectByUserId(@Param("userId") Long userId);

    int selectNoteIsLiked(@Param("userId") Long userId, @Param("noteId") Long noteId);

    List<NoteLikePO> selectLikedByUserIdAndLimit(@Param("userId") Long userId, @Param("limit")  int limit);

    int insertOrUpdate(NoteLikePO noteLikePO);

    int update2UnlikeByUserIdAndNoteId(NoteLikePO noteLikePO);


}