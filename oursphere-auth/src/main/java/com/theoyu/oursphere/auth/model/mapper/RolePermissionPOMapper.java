package com.theoyu.oursphere.auth.model.mapper;

import com.theoyu.oursphere.auth.model.entity.RolePermissionPO;

public interface RolePermissionPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(RolePermissionPO record);

    int insertSelective(RolePermissionPO record);

    RolePermissionPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RolePermissionPO record);

    int updateByPrimaryKey(RolePermissionPO record);
}