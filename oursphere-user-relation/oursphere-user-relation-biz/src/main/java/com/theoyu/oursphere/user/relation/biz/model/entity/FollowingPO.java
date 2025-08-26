package com.theoyu.oursphere.user.relation.biz.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowingPO {
    private Long id;

    private Long userId;

    private Long followingUserId;

    private LocalDateTime createTime;


}