package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.params.AuthParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    private XcUserMapper xcUserMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private XcMenuMapper xcMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AuthParamsDto authParamsDto = null;
        try{
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        }catch(Exception e){
            log.error("认证请求参数格式不对");
            throw new RuntimeException("认证请求参数格式不对");
        }

        String username = authParamsDto.getUsername();
        //查询用户信息
        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername,username));
        if(user == null){
            //用户不存在，直接返回
            return null;
        }

        //开始认证
        String beanName = authParamsDto.getAuthType()+"_authservice";
        AuthService authService = applicationContext.getBean(beanName,AuthService.class);
        XcUserExt xcUserExt = authService.execute(authParamsDto);

        UserDetails userDetails = getUserPrinciple(xcUserExt);
        return userDetails;
    }

    private UserDetails getUserPrinciple(XcUserExt xcUserExt){
        //1.获取用户权限数组
        String userId = xcUserExt.getId();
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(userId);
        List<String> permissions = new ArrayList<>();
        if(xcMenus.size()<=0){
            //用户权限数组大小必须大于0
            permissions.add("test");
        }else{
            xcMenus.forEach(menu->{
                permissions.add(menu.getCode());
            });
        }

        //2.创建UserDetails对象
        String password = xcUserExt.getPassword();
        String username = xcUserExt.getUsername();
        String[] authorities = permissions.toArray(new String[0]);

        //为了安全在令牌中不放密码
        xcUserExt.setPassword(null);

        //将用户数据转为json
        String userJson = JSON.toJSONString(xcUserExt);
        UserDetails userdetails = User.withUsername(userJson).password(password).authorities(authorities).build();

        return userdetails;
    }
}
