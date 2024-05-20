package com.xuecheng.ucenter.service.impl;

import com.alibaba.nacos.common.model.RestResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.auth.feignclient.CheckcodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.RegisterParams;
import com.xuecheng.ucenter.model.po.XcRole;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("registerService")
public class RegisterServiceImpl implements RegisterService {
    @Autowired
    private CheckcodeClient checkcodeClient;
    @Autowired
    private XcUserMapper userMapper;
    @Autowired
    private XcUserRoleMapper userRoleMapper;

    @Override
    public RestResult register(RegisterParams registerParams) {
        //判断验证码是否正确
        String key = registerParams.getCheckcodekey();
        String checkcode = registerParams.getCheckcode();
        Boolean verify = checkcodeClient.verify(key, checkcode);
        if(!verify){
            throw new RuntimeException("验证码不正确！");
        }
        //判断密码输入是否相同
        String password = registerParams.getPassword();
        String confirmpwd = registerParams.getConfirmpwd();
        if(!password.equals(confirmpwd)){
            throw new RuntimeException("两次输入密码不匹配！");
        }
        String phone = registerParams.getCellphone();
        String email = registerParams.getEmail();
        String nickname = registerParams.getNickname();
        //判断用户是否存在
        LambdaQueryWrapper<XcUser> lqw = new LambdaQueryWrapper<XcUser>().eq(XcUser::getCellphone,phone);
        XcUser xcUser = userMapper.selectOne(lqw);
        if(xcUser!=null) throw new RuntimeException("该用户已存在！");

        //将信息存入用户表和用户角色表
        XcUser user = new XcUser();
        user.setCellphone(phone);
        user.setEmail(email);
        user.setNickname(nickname);
        userMapper.insert(user);
        XcUserRole userRole = new XcUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId("20");
        userRoleMapper.insert(userRole);

        return new RestResult(200,"注册成功");
    }
}
