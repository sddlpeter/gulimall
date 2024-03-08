package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.feign.ThirdPartyFeignService;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegistVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;
    // SpringMVC viewController 将请求映射到页面，这样就不用写下面的空方法了
//    @GetMapping("/login.html")
//    public String loginPage() {
//        return "login";
//    }
    @Autowired
    MemberFeignService memberFeignService;

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {

        // TODO 1. 接口防刷
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000) {
                // 60s内不能再发
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        // 2. 验证码校验
        int num = (int) (1000 + Math.random() * 10000);
        String numCode = String.valueOf(num);
        String code = numCode + "_" + System.currentTimeMillis();
        System.out.println(" ========= 短信验证码是：" + numCode + " ===========");

        // 包含过期时间，存入redis, 防止同一个手机号在60s内再次发送验证码
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code, 10, TimeUnit.MINUTES);

        // TODO 先注释发短信服务，记得打开
        thirdPartyFeignService.sendCode(phone, numCode);
        return R.ok();
    }

    @PostMapping("/regist")
    public String register(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(
                    Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            attributes.addFlashAttribute("errors", errors);
            // 校验出错，转发到注册页 -> 重定向到注册页
            // return "forward:/reg.html";
            // return "reg";
            // return "redirect:reg.html";  // 默认重定向到ip地址+端口的形式，需要改成域名的形式：auth.gulimall.com/reg.html
            return "redirect:http://auth.gulimall.com/reg.html";
            // Request method 'POST' not supported
            // 用户注册 -> /regist[post] --> 转发/reg.html (路径映射默认都是get方式访问的)
        }
        // 1. 校验验证码
        String code = vo.getCode();
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (!StringUtils.isEmpty(s)) {
            if (code.equals(s.split("_")[0])) {
                // 删除验证码, 令牌机制
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());

                // 验证码正确
                // 真正注册, 调用远程服务注册
                R r = memberFeignService.regist(vo);
                if(r.getCode() == 0) {
                    // 成功
                    return "redirect:http://auth.gulimall.com/login.html";
                }else {
                    //失败
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData(new TypeReference<String>(){}));
                    attributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            } else {
                // 验证码错误，转发注册页面
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码不正确");
                attributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            // 校验出错，转发注册页面
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码过期或不存在");
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo) {
        return "";
    }
}
