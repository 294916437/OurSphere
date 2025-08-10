package com.theoyu.oursphere.auth.controller;


import cn.dev33.satoken.stp.StpUtil;
import com.theoyu.framework.common.response.Response;
import com.theoyu.framework.logger.aspect.ApiOperationLog;
import com.theoyu.oursphere.auth.model.vo.user.UserLoginReqVO;
import com.theoyu.oursphere.auth.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Resource
    private  UserService userService;

    @PostMapping("/login")
    @ApiOperationLog(description = "登录与注册")
    public Response<String> loginAndRegister(@RequestBody UserLoginReqVO userLoginReqVO) {
        return userService.loginAndRegister(userLoginReqVO);
    }

    @PostMapping("/logout")
    @ApiOperationLog(description = "登出")
    public Response<?> logout() {

        // todo 账号退出登录逻辑待实现
        StpUtil.logout();
        return Response.success();
    }
}
