package com.xuecheng.system;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * <p>
 *     系统管理启动类
 * </p>
 *
 * @Description:
 */
@EnableScheduling
@EnableSwagger2Doc
@SpringBootApplication
public class SystemApplication {
    @Value("${spring.datasource.username}")
    private static String pwd;
    @Value("${spring.datasource.url}")
    private static String url;

    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class,args);
        //show();
        System.out.println(pwd);
        System.out.println(url);
    }
//    static void show(){
//        System.out.println(pwd);
//        System.out.println(url);
//    }
}