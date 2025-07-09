package com.theoyu.oursphere.auth.model.mapper;

import com.theoyu.oursphere.auth.model.entity.PermissionPO;

public interface PermissionPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(PermissionPO record);

    int insertSelective(PermissionPO record);

    PermissionPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PermissionPO record);

    int updateByPrimaryKey(PermissionPO record);
}