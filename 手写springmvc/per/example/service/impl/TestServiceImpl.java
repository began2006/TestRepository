package per.example.service.impl;

import per.example.annotation.Service_gk;
import per.example.service.TestService;

/**
 * @description:
 * @author: gk
 * @date: 2019/5/31 13:47
 * @since: jdk1.8
 */

@Service_gk("testService")
public class TestServiceImpl implements TestService {
    @Override
    public String hello() {
        return "hello world";
    }
}
