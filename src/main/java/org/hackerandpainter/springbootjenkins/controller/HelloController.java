package org.hackerandpainter.springbootjenkins.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @Author Gao Hang Hang
 * @Date 2019-07-31 22:33
 **/
@RestController
public class HelloController {

    @GetMapping("/")
    public String hello() {
        //return "hello";
        //return "hello new";
        //return "hello new new";
        return "hello 你好";
    }
}
