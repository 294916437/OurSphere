package com.theoyu.oursphere.user.relation.biz.controller;

import com.theoyu.framework.common.response.Response;
import com.theoyu.framework.logger.aspect.ApiOperationLog;
import com.theoyu.oursphere.user.relation.biz.model.vo.FollowUserReqVO;
import com.theoyu.oursphere.user.relation.biz.service.RelationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relation")
@Slf4j
public class RelationController {
    @Resource
    private RelationService relationService;

    @PostMapping("/follow")
    @ApiOperationLog(description = "关注用户")
    public Response<?> follow(@Validated @RequestBody FollowUserReqVO followUserReqVO) {
        return relationService.follow(followUserReqVO);
    }
}
