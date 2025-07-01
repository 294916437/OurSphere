package com.theoyu.oursphere.auth.domain.mapper;

import com.theoyu.oursphere.auth.domain.dataobject.UserDo;

public interface UserDoMapper {

    /**
     * 根据主键 ID 查询
     * @param id
     * @return
     */
    UserDo selectByPrimaryKey(Long id);

    /**
     * 根据主键 ID 删除
     * @param id
     * @return
     */
    int deleteByPrimaryKey(Long id);

    /**
     * 插入记录
     * @param record
     * @return
     */
    int insert(UserDo record);

    /**
     * 更新记录
     * @param record
     * @return
     */
    int updateByPrimaryKey(UserDo record);
}
