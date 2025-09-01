package com.theoyu.oursphere.count.biz.model.mapper;

import com.theoyu.oursphere.count.biz.model.entity.UserCountPO;
import org.apache.ibatis.annotations.Param;

public interface UserCountPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserCountPO record);

    int insertSelective(UserCountPO record);

    UserCountPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserCountPO record);

    int updateByPrimaryKey(UserCountPO record);

    int insertOrUpdateFansTotalByUserId(@Param("count") Integer count, @Param("userId") Long userId);

    int insertOrUpdateFollowingTotalByUserId(@Param("count") Integer count, @Param("userId") Long userId);

    int insertOrUpdateLikeTotalByUserId(@Param("count") Integer count, @Param("userId") Long userId);

    int insertOrUpdateNoteTotalByUserId(@Param("count") Long count, @Param("userId") Long userId);

    int insertOrUpdateCollectTotalByUserId(@Param("count") Integer count, @Param("userId") Long userId);


}