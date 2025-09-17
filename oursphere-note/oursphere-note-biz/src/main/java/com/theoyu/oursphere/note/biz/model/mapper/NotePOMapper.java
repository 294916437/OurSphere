package com.theoyu.oursphere.note.biz.model.mapper;

import com.theoyu.oursphere.note.biz.model.entity.NoteLikePO;
import com.theoyu.oursphere.note.biz.model.entity.NotePO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NotePOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NotePO record);

    int insertSelective(NotePO record);

    NotePO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NotePO record);

    int updateVisibleOnlyMe(NotePO notePO);

    int updateIsTop(NotePO notePO);

    int selectCountByNoteId(Long noteId);

    Long selectCreatorIdByNoteId(Long noteId);






}