package com.theoyu.oursphere.user.biz.model.mapper;

import com.theoyu.oursphere.user.biz.model.entity.PermissionPO;

import java.util.List;

public interface PermissionPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(PermissionPO record);

    int insertSelective(PermissionPO record);

    PermissionPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PermissionPO record);

    int updateByPrimaryKey(PermissionPO record);

    /**
     * 查询全部启用的权限
     *
     * @return
     */
    List<PermissionPO> selectEnabledPermissions();
}