package com.xuecheng;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class XuechengPlusGatewayApplicationTests {

    @Value("${spring.application.name}")
    String name;

    @Test
    void contextLoads() {
        System.out.println(name);
    }

}
