package com.github.hbq969.code.zkc.control;

import com.github.hbq969.code.common.encrypt.ext.config.Decrypt;
import com.github.hbq969.code.common.restful.ReturnMessage;
import com.github.hbq969.code.zkc.model.UserInfo;
import com.github.hbq969.code.zkc.service.SystemService;
import com.github.hbq969.code.zkc.view.request.LoginInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("config-web-LoginCtrl")
@RequestMapping(path = "/hbq969-zkc/system")
@Slf4j
@Api(tags = "配置中心-登录管理")
public class LoginCtrl {

    @Autowired
    private SystemService systemService;

    @ApiOperation("登录")
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    @ResponseBody
    @Decrypt
    public ReturnMessage<?> login(HttpServletRequest request, HttpServletResponse response, @RequestBody LoginInfo info) {
        systemService.login(info, request, response);
        return ReturnMessage.success("登录成功");
    }

    @ApiOperation("注销")
    @RequestMapping(path = "/logout", method = RequestMethod.POST)
    @ResponseBody
    public ReturnMessage<?> logout(HttpServletRequest request, HttpServletResponse response) {
        systemService.logout(request, response);
        return ReturnMessage.success("注销成功");
    }

    @ApiOperation("获取账号信息")
    @RequestMapping(path = "/user", method = RequestMethod.GET)
    @ResponseBody
    public ReturnMessage<com.github.hbq969.code.sm.login.model.UserInfo> getUserInfo(HttpServletRequest request) {
        return ReturnMessage.success(systemService.getUserInfo(request));
    }
}
