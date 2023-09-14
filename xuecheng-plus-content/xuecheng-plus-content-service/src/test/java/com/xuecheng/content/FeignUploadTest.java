package com.xuecheng.content;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Slf4j
@SpringBootTest
public class FeignUploadTest {
    @Autowired
    private MediaServiceClient mediaServiceClient;

    @Test
    void test(){
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("D:\\temp\\test.html"));
        String upload = mediaServiceClient.uploadFile(multipartFile, "course/test.html");
        if(upload == null){
            log.debug("发生熔断");
        }
    }
}
