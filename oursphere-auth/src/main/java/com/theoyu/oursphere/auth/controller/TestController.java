package com.theoyu.oursphere.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.theoyu.framework.logger.aspect.ApiOperationLog;
import org.springframework.web.bind.annotation.*;
import com.theoyu.framework.common.response.Response;
import java.time.LocalDateTime;

@RestController
public class TestController {
    // 测试登录，浏览器访问： http://localhost:8080/user/doLogin?username=zhang&password=123456
    @RequestMapping("/user/doLogin")
    public String doLogin(String username, String password) {
        // 此处仅作模拟示例，真实项目需要从数据库中查询数据进行比对
        if("zhang".equals(username) && "123456".equals(password)) {
            StpUtil.login(10001);
            return "登录成功";
        }
        return "登录失败";
    }

    // 查询登录状态，浏览器访问： http://localhost:8080/user/isLogin
    @RequestMapping("/user/isLogin")
    public String isLogin() {
        return "当前会话是否登录：" + StpUtil.isLogin();
    }

}
