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
public class NotePO {
    private Long id;

    private String title;

    private Boolean isContentEmpty;

    private Long creatorId;

    private Long topicId;

    private String topicName;

    private Boolean isTop;

    private Byte type;

    private String imgUris;

    private String videoUri;

    private Byte visible;

    private LocalDateTime  createTime;

    private LocalDateTime  updateTime;

    private Byte status;

    private String contentUuid;

    private String topicIds;

    private Long channelId;

}