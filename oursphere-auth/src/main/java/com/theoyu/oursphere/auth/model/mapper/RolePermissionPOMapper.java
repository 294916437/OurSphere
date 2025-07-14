package com.theoyu.oursphere.auth.model.mapper;

import com.theoyu.oursphere.auth.model.entity.RolePermissionPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RolePermissionPOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(RolePermissionPO record);

    int insertSelective(RolePermissionPO record);

    RolePermissionPO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RolePermissionPO record);

    int updateByPrimaryKey(RolePermissionPO record);

    /**
     * 根据角色 ID 查询角色对应的权限集合
     *
     * @param roleIds 角色 ID 集合
     * @return 角色对应的权限集合
     */
    List<RolePermissionPO> selectByRoleIds(@Param("roleIds") List<Long> roleIds);
}