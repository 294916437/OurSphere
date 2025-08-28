package com.theoyu.oursphere.user.relation.biz.model.mapper;

import com.theoyu.oursphere.user.relation.biz.model.entity.FansPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface FansPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FansPO record);

    int insertSelective(FansPO record);

    FansPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FansPO record);

    int updateByPrimaryKey(FansPO record);
    int deleteByUserIdAndFansUserId(@Param("userId") Long userId,
                                    @Param("fansUserId") Long fansUserId);
    long selectCountByUserId(Long userId);
    /**
     * 分页查询
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<FansPO> selectPageListByUserId(@Param("userId") Long userId,
                                        @Param("offset") long offset,
                                        @Param("limit") long limit);
    /**
     * 查询最新关注的 5000 位粉丝
     * @param userId
     * @return
     */
    List<FansPO> select5000FansByUserId(Long userId);


}