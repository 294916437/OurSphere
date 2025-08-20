package com.theoyu.oursphere.user.biz.model.mapper;

import com.theoyu.oursphere.user.biz.model.entity.UserPO;

public interface UserPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserPO record);

    int insertSelective(UserPO record);

    UserPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserPO record);

    int updateByPrimaryKey(UserPO record);

    /**
     * 根据手机号查询用户密码
     * @param phone
     * @return
     */
    UserPO selectPwdByPhone(String phone);
}