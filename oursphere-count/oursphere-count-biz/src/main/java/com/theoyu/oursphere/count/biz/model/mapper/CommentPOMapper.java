package com.theoyu.oursphere.count.biz.model.mapper;

import com.theoyu.oursphere.count.biz.model.entity.CommentPO;
import org.apache.ibatis.annotations.Param;

public interface CommentPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CommentPO record);

    int insertSelective(CommentPO record);

    CommentPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CommentPO record);

    int updateByPrimaryKey(CommentPO record);

    int updateChildCommentTotal(@Param("parentId") Long parentId, @Param("count") int count);

}