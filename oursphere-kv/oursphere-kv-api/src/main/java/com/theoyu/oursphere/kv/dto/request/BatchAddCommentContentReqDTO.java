package com.theoyu.oursphere.kv.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BatchAddCommentContentReqDTO {

    @NotEmpty(message = "评论内容集合不能为空")
    @Valid  // 指定集合内的评论 DTO，也需要进行参数校验
    private List<CommentContentReqDTO> comments;

}