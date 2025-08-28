package com.theoyu.oursphere.user.biz.model.mapper;

import com.theoyu.oursphere.user.biz.model.entity.UserPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserPO record);

    int insertSelective(UserPO record);

    UserPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserPO record);

    int updateByPrimaryKey(UserPO record);

    /**
     * 根据手机号查询用户密码
     *
     * @param phone
     * @return
     */
    UserPO selectByPhone(String phone);
    /**
     * 批量查询用户信息
     *
     * @param ids
     * @return
     */
    List<UserPO> selectByIds(@Param("ids") List<Long> ids);
}