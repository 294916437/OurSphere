package com.theoyu.oursphere.auth.controller;

import com.theoyu.framework.common.response.Response;
import com.theoyu.framework.logger.aspect.ApiOperationLog;
import com.theoyu.oursphere.auth.model.vo.user.UserLoginReqVO;
import com.theoyu.oursphere.auth.model.vo.user.UserPasswordReqVO;
import com.theoyu.oursphere.auth.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
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
        return userService.logout();
    }

    @PostMapping("/password/update")
    @ApiOperationLog(description = "修改密码")
    public Response<?> updatePassword(@Validated @RequestBody UserPasswordReqVO updatePasswordReqVO) {
        return userService.updatePassword(updatePasswordReqVO);
    }
}
