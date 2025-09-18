package com.theoyu.oursphere.comment.biz.model.mapper;

import com.theoyu.oursphere.comment.biz.model.entity.NoteCountPO;


public interface NoteCountPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteCountPO record);

    int insertSelective(NoteCountPO record);

    NoteCountPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteCountPO record);

    int updateByPrimaryKey(NoteCountPO record);
    /**
     * 查询笔记评论总数
     * @param noteId
     * @return
     */
    Long selectCommentTotalByNoteId(Long noteId);


}