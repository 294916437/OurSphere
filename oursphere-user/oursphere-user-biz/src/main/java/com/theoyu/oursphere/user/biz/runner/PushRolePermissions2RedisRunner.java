package com.theoyu.oursphere.user.biz.runner;


import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.theoyu.framework.common.utils.JsonUtils;
import com.theoyu.oursphere.user.biz.constants.RedisKeyConstants;
import com.theoyu.oursphere.user.biz.model.entity.PermissionPO;
import com.theoyu.oursphere.user.biz.model.entity.RolePO;
import com.theoyu.oursphere.user.biz.model.entity.RolePermissionPO;
import com.theoyu.oursphere.user.biz.model.mapper.PermissionPOMapper;
import com.theoyu.oursphere.user.biz.model.mapper.RolePOMapper;
import com.theoyu.oursphere.user.biz.model.mapper.RolePermissionPOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 启动时将角色权限数据同步到 Redis 中
 * 主要用于缓存角色对应的权限信息
 */
@Component
@Slf4j
public class PushRolePermissions2RedisRunner implements ApplicationRunner {

    @Resource
    private final RolePOMapper rolePOMapper;
    @Resource
    private RolePermissionPOMapper rolePermissionPOMapper;
    @Resource
    private PermissionPOMapper permissionPOMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PUSH_PERMISSION_FLAG = "push.permission.flag";


    public PushRolePermissions2RedisRunner(RolePOMapper rolePOMapper) {
        this.rolePOMapper = rolePOMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("==> 服务启动，开始同步角色权限数据到 Redis 中...");

        try {
            // 是否已经同步过角色权限数据到 Redis 中
            // 判断逻辑：只有在键 PUSH_PERMISSION_FLAG 不存在时，才会设置该键的值为 "1"，并设置过期时间为 1 hour
            boolean canPushed = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(PUSH_PERMISSION_FLAG, "1", 1, TimeUnit.HOURS));

            // 无法同步权限数据
            if (!canPushed) {
                log.warn("==> 角色权限数据已经同步至 Redis 中，不再同步...");
                return;
            }

            List<RolePO> roles = rolePOMapper.selectEnabledRoles();
            if (CollUtil.isEmpty(roles)) {
                log.info("==> 没有启用的角色数据，跳过同步角色权限数据到 Redis 中...");
                return;
            } else {
                List<Long> roleIds = roles.stream().map(RolePO::getId).toList();
                List<RolePermissionPO> rolePermissionPOS = rolePermissionPOMapper.selectByRoleIds(roleIds);

                // 每个角色 ID 对应多个权限 ID
                Map<Long, List<Long>> rolePermissionsMap = rolePermissionPOS.stream().collect(
                        Collectors.groupingBy(RolePermissionPO::getRoleId,
                                Collectors.mapping(RolePermissionPO::getPermissionId, Collectors.toList()))
                );

                List<PermissionPO> permissionPOS = permissionPOMapper.selectEnabledPermissions();

                //权限 ID 到权限 PO 对象的映射
                Map<Long, PermissionPO> permissionsMap = permissionPOS.stream().collect(
                        Collectors.toMap(PermissionPO::getId, permissionPO -> permissionPO)
                );

                // 角色ID-权限PO 对象的关系
                Map<String, List<String>> roleIdPermissionMap = Maps.newHashMap();

                // 遍历所有角色，
                roles.forEach(rolePO -> {
                    // 当前角色 ID
                    Long roleId = rolePO.getId();
                    // 当前角色 roleKey
                    String roleKey = rolePO.getRoleKey();
                    // 当前角色 ID 对应的权限 ID 集合
                    List<Long> permissionIds = rolePermissionsMap.get(roleId);
                    if (CollUtil.isNotEmpty(permissionIds)) {
                        List<String> permissionKeys = Lists.newArrayList();
                        permissionIds.forEach(permissionId -> {
                            // 根据权限 ID 获取具体的权限 DO 对象
                            PermissionPO permissionDO = permissionsMap.get(permissionId);
                            permissionKeys.add(permissionDO.getPermissionKey());
                        });
                        roleIdPermissionMap.put(roleKey, permissionKeys);
                    }
                });

                //实现Redis缓存
                roleIdPermissionMap.forEach((roleKey, permissions) -> {
                    String key = RedisKeyConstants.buildRolePermissionsKey(roleKey);
                    redisTemplate.opsForValue().set(key, JsonUtils.toJsonString(permissions));
                });

            }


            log.info("==> 服务启动，成功同步角色权限数据到 Redis 中...");
        } catch (Exception e) {
            log.error("==> 同步角色权限数据到 Redis 中失败: ", e);
        }


    }
}

