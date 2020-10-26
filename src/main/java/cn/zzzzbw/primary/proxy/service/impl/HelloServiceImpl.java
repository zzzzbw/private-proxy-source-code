package cn.zzzzbw.primary.proxy.service.impl;

import cn.zzzzbw.primary.proxy.service.HelloService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author by zzzzbw
 * @since 2020/09/15 9:52
 */
@Slf4j
@Transactional
@Service
public class HelloServiceImpl implements HelloService {

    @Override
    public void hello(String name) {
        log.info("hello {}!", name);
    }

    private long privateHello(Integer time) {
        log.info("private hello! time: {}", time);
        return System.currentTimeMillis();
    }
}
