package com.xuecheng;

import com.spring4all.swagger.EnableSwagger2Doc;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = {"com.xuecheng.content.feignclient"})
@SpringBootApplication
@EnableSwagger2Doc //允许生成接口文档
public class ContentApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentApiApplication.class, args);
    }
}
