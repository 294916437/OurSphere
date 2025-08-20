package com.theoyu.oursphere.user.biz.model.mapper;

import com.theoyu.oursphere.user.biz.model.entity.RolePO;

import java.util.List;

public interface RolePOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(RolePO record);

    int insertSelective(RolePO record);

    RolePO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RolePO record);

    int updateByPrimaryKey(RolePO record);

    /**
     * 查询全部启用的角色
     *
     * @return
     */
    List<RolePO> selectEnabledRoles();
}