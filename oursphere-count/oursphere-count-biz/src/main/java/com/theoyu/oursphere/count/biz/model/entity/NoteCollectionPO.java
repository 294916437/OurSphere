package com.theoyu.oursphere.count.biz.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoteCollectionPO {
    private Long id;

    private Long userId;

    private Long noteId;

    private LocalDateTime createTime;

    private Integer  status;


}