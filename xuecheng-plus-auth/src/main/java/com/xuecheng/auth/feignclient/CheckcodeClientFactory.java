package com.xuecheng.auth.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CheckcodeClientFactory implements FallbackFactory<CheckcodeClient> {

    @Override
    public CheckcodeClient create(Throwable throwable) {
        return new CheckcodeClient() {
            @Override
            public Boolean verify(String key, String code) {
                log.debug("远程调用验证码服务发生熔断");
                return null;
            }
        };
    }
}
