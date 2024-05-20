package com.xuecheng.auth.controller;

import com.alibaba.nacos.common.model.RestResult;
import com.xuecheng.ucenter.model.dto.RegisterParams;
import com.xuecheng.ucenter.service.RegisterService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

//@RestController
public class RegisterController {

    @Resource(name="registerService")
    private RegisterService registerService;

    @PostMapping("/register")
    public RestResult register(RegisterParams registerParams){
        return registerService.register(registerParams);
    }
}
