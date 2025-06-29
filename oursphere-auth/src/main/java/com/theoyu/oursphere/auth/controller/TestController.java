package com.theoyu.oursphere.auth.controller;

import com.theoyu.framework.logger.aspect.ApiOperationLog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.theoyu.framework.common.response.Response;
import java.time.LocalDateTime;


@RestController
public class TestController {
    @GetMapping("/test")
    @ApiOperationLog(description = "测试接口")
    public Response<String> test() {
        return Response.success("Hello,World!");
    }

    @GetMapping("/test2")
    @ApiOperationLog(description = "测试接口2")
    public Response<User> test2() {
        return Response.success(User.builder()
                .nickName("犬小哈")
                .createTime(LocalDateTime.now())
                .build());
    }
}
