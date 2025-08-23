package com.theoyu.oursphere.note.biz.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChannelPO {
    private Long id;

    private String name;

    private LocalDateTime  createTime;

    private LocalDateTime  updateTime;

    private Boolean isDeleted;

}