package com.theoyu.oursphere.comment.biz.model.mapper;

import com.theoyu.oursphere.comment.biz.model.bo.CommentBO;
import com.theoyu.oursphere.comment.biz.model.bo.CommentHeatBO;
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

    /**
     * 批量更新热度值
     * @param commentIds
     * @param commentHeatBOS
     * @return
     */
    int batchUpdateHeatByCommentIds(@Param("commentIds") List<Long> commentIds,
                                    @Param("commentHeatBOS") List<CommentHeatBO> commentHeatBOS);

    /**
     * 查询一级评论下最早回复的评论
     * @param parentId
     * @return
     */
    CommentPO selectLatestByParentId(Long parentId);
    /**
     * 更新一级评论的 first_reply_comment_id
     * @param firstReplyCommentId
     * @param id
     * @return
     */
    int updateFirstReplyCommentIdByPrimaryKey(@Param("firstReplyCommentId") Long firstReplyCommentId,
                                              @Param("id") Long id);
    /**
     * 查询评论分页数据
     * @param noteId
     * @param offset
     * @param pageSize
     * @return
     */
    List<CommentPO> selectPageList(@Param("noteId") Long noteId,
                                   @Param("offset") long offset,
                                   @Param("pageSize") long pageSize);
    /**
     * 批量查询二级评论
     * @param commentIds
     * @return
     */
    List<CommentPO> selectTwoLevelCommentByIds(@Param("commentIds") List<Long> commentIds);



}