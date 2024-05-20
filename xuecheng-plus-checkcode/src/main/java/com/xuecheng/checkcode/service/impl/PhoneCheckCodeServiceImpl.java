package com.xuecheng.checkcode.service.impl;

import com.sun.xml.internal.bind.v2.TODO;
import com.xuecheng.checkcode.model.CheckCodeParamsDto;
import com.xuecheng.checkcode.model.CheckCodeResultDto;
import com.xuecheng.checkcode.service.AbstractCheckCodeService;
import com.xuecheng.checkcode.service.CheckCodeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("PhoneCheckCodeService")
public class PhoneCheckCodeServiceImpl extends AbstractCheckCodeService implements CheckCodeService {
    @Resource(name="NumberLetterCheckCodeGenerator")
    @Override
    public void setCheckCodeGenerator(CheckCodeGenerator checkCodeGenerator) {
        this.checkCodeGenerator = checkCodeGenerator;
    }

    @Resource(name="UUIDKeyGenerator")
    @Override
    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }


    @Resource(name="RedisCheckCodeStore")
    @Override
    public void setCheckCodeStore(CheckCodeStore checkCodeStore) {
        this.checkCodeStore = checkCodeStore;
    }

    @Override
    public CheckCodeResultDto generate(CheckCodeParamsDto checkCodeParamsDto) {
        //生成6位短信验证码
        GenerateResult generate = generate(checkCodeParamsDto,6,"checkcode",60);
        String key = generate.getKey();
        String code = generate.getCode();
        //TODO:send code to the phone
        String phone = checkCodeParamsDto.getParam1();
        System.out.println("验证码："+code);
        //返回结果
        CheckCodeResultDto checkCodeResultDto = new CheckCodeResultDto();
        checkCodeResultDto.setKey(key);
        return checkCodeResultDto;
    }
}
