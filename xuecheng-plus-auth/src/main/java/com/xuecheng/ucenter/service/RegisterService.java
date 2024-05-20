package com.xuecheng.ucenter.service;

import com.alibaba.nacos.common.model.RestResult;
import com.xuecheng.ucenter.model.dto.RegisterParams;
import org.springframework.stereotype.Service;


public interface RegisterService {
    public RestResult register(RegisterParams registerParams);
}
