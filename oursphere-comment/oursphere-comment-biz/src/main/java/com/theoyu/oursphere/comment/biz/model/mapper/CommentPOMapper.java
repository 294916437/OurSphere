package com.theoyu.oursphere.comment.biz.model.mapper;

import com.theoyu.oursphere.comment.biz.model.entity.CommentPO;

public interface CommentPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CommentPO record);

    int insertSelective(CommentPO record);

    CommentPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CommentPO record);

    int updateByPrimaryKey(CommentPO record);
}