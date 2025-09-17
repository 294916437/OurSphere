package com.theoyu.oursphere.comment.biz.model.mapper;

import com.theoyu.oursphere.comment.biz.model.entity.CommentLikePO;

public interface CommentLikePOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CommentLikePO record);

    int insertSelective(CommentLikePO record);

    CommentLikePO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CommentLikePO record);

    int updateByPrimaryKey(CommentLikePO record);
}