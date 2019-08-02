package per.example.controller;

import per.example.annotation.Autowired_gk;
import per.example.annotation.Controller_gk;
import per.example.annotation.RequestMapping_gk;
import per.example.service.TestService;

/**
 * @description:
 * @author: gk
 * @date: 2019/5/30 15:44
 * @since: jdk1.8
 */

@Controller_gk("testController")
@RequestMapping_gk("/ServletTest")
public class TestController {

    @Autowired_gk("testService")
    private TestService testService;

    @RequestMapping_gk("/hello")
    public String test() {
        System.out.println("----------------- abc -----------------");
        return testService.hello();
    }
}
