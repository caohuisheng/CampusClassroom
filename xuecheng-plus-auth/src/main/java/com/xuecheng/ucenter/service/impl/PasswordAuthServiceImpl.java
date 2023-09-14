package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.auth.feignclient.CheckcodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService {

    @Autowired
    private XcUserMapper xcUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CheckcodeClient checkcodeClient;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        //1.校验验证码
        String checkcode = authParamsDto.getCheckcode();
        String checkcodekey = authParamsDto.getCheckcodekey();
//        if(StringUtils.isEmpty(checkcode)||StringUtils.isEmpty(checkcodekey)){
//            throw new RuntimeException("验证码为空");
//        }
//        Boolean verify = checkcodeClient.verify(checkcodekey, checkcode);
//        if(!verify){
//            throw new RuntimeException("验证码错误");
//        }

        //2.校验用户名和密码
        String username = authParamsDto.getUsername();
        //查询用户信息
        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername,username));
        if(user == null){
            //用户不存在，直接返回
            throw new RuntimeException("账号不存在");
        }

        //校验密码
        String passwordDb = user.getPassword();
        String passwordAuth = authParamsDto.getPassword();
        boolean matches = passwordEncoder.matches(passwordAuth, passwordDb);
        if(!matches){
            throw new RuntimeException("账号或密码错误");
        }

        //3.返回用户信息
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user,xcUserExt);
        return xcUserExt;
    }
}
