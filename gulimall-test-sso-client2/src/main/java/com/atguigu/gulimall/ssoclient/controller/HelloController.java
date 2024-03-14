package com.atguigu.gulimall.ssoclient.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HelloController {

    @Value("${sso.server.url}")
    String ssoServerUrl;



    @ResponseBody
    @GetMapping("/hello")
    public String hello() {

        return "hello";
    }

    @GetMapping("/boss")
    public String employees(Model model, HttpSession session, @RequestParam(value="token", required = false) String token) {
        if (!StringUtils.isEmpty(token)) {
            // TODO 去ssoserver获取当前token对应的用户信息
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> forEntity = restTemplate.getForEntity("http://ssoserver.com:8080/userInfo?token=" + token, String.class);
            String body = forEntity.getBody();
            session.setAttribute("loginUser", body );
        }

        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:" + ssoServerUrl + "?redirect_url=http://client2.com:8082/boss";
        } else {
            List<String> emps = new ArrayList<>();
            emps.add("zhangsan");
            emps.add("lisi");
            model.addAttribute("emps", emps);
            return "list";
        }

    }
}
