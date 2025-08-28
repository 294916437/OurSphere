package com.theoyu.oursphere.user.relation.biz.model.mapper;

import com.theoyu.oursphere.user.relation.biz.model.entity.FollowingPO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface FollowingPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FollowingPO record);

    int insertSelective(FollowingPO record);

    FollowingPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FollowingPO record);

    int updateByPrimaryKey(FollowingPO record);

    List<FollowingPO> selectByUserId(Long userId);

    int deleteByUserIdAndFollowingUserId(@Param("userId") Long userId,
                                         @Param("unfollowUserId") Long unfollowUserId);

    long selectCountByUserId(Long userId);

    /**
     * 分页查询
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<FollowingPO> selectPageListByUserId(@Param("userId") Long userId,
                                             @Param("offset") long offset,
                                             @Param("limit") long limit);
    /**
     * 查询关注用户列表
     * @param userId
     * @return
     */
    List<FollowingPO> selectAllByUserId(Long userId);
}