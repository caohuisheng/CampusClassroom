package com.xuecheng.auth.controller;

import com.alibaba.nacos.common.model.RestResult;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.RegisterParams;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.RegisterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Mr.M
 * @version 1.0
 * @description 测试controller
 * @date 2022/9/27 17:25
 */
@Slf4j
@RestController
public class LoginController {

    @Autowired
    XcUserMapper userMapper;

    @RequestMapping("/test")
    public String test(){
        return "hello";
    }

    @Resource(name="registerService")
    private RegisterService registerService;

    @RequestMapping("/register")
    public RestResult register(RegisterParams registerParams){
        System.out.println("register...");
        return registerService.register(registerParams);
    }

    @RequestMapping("/login-successa")
    public String loginSuccess() {
        return "登录成功";
    }

    @RequestMapping("/user/{id}")
    public XcUser getuser(@PathVariable("id") String id) {
        XcUser xcUser = userMapper.selectById(id);
        return xcUser;
    }

    @RequestMapping("/r/r1")
    @PreAuthorize("hasAuthority('p1')")
    public String r1() {
        return "访问r1资源";
    }

    @RequestMapping("/r/r2")
    @PreAuthorize("hasAuthority('p2')")
    public String r2() {
        return "访问r2资源";
    }

}
