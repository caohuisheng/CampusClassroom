package com.xuecheng.auth.controller;

import com.alibaba.nacos.common.model.RestResult;
import com.xuecheng.ucenter.model.dto.RegisterParams;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.RegisterService;
import com.xuecheng.ucenter.service.WxAuthService;
import com.xuecheng.ucenter.service.impl.WxAuthServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Controller
public class WxLoginController {

    @Autowired
    private WxAuthService wxAuthService;

    @RequestMapping("/wxLogin")
    public String wxLogin(String code, String state) throws IOException {
        log.debug("微信扫码回调,code:{},state:{}",code,state);
        //请求微信申请令牌，拿到令牌查询用户信息，将用户信息写入本项目数据库
        XcUser xcUser = wxAuthService.wxAuth(code);
        if(xcUser == null){
            return "redirect:http://www.51xuecheng.cn/error.html";
        }

        String username = xcUser.getUsername();
        return "redirect:http://www.51xuecheng.cn/sign.html?username="+username+"&authType=wx";
    }
}
