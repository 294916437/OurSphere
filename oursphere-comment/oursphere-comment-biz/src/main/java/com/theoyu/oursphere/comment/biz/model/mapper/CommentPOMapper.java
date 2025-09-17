package com.theoyu.oursphere.comment.biz.model.mapper;

import com.theoyu.oursphere.comment.biz.model.bo.CommentBO;
import com.theoyu.oursphere.comment.biz.model.entity.CommentPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CommentPO record);

    int insertSelective(CommentPO record);

    CommentPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CommentPO record);

    int updateByPrimaryKey(CommentPO record);
    /**
     * 根据评论 ID 批量查询
     * @param commentIds
     * @return
     */
    List<CommentPO> selectByCommentIds(@Param("commentIds") List<Long> commentIds);

    /**
     * 批量插入评论
     * @param comments
     * @return
     */
    int batchInsert(@Param("comments") List<CommentBO> comments);

}