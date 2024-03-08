package com.atguigu.gulimall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// @SpringBootTest
class GulimallMemberApplicationTests {

    @Test
    void contextLoads() {
        // 默认md5
        String s = DigestUtils.md5Hex("123456");
        System.out.println(s);

        //盐值加密 -> 保存用户信息的时候，既要保存hash之后的密码，又要保存随机盐到数据库
        String s1 = Md5Crypt.md5Crypt("123456".getBytes(), "$1$qqqqqqqq");
        System.out.println(s1);


        //$2a$10$8yDjAObDEtr20mZ9zDsSreaFTntqWhU/ct0ZgJpsxvhyGavzI0IFe
        //$2a$10$NPvVw/DeZKSrLcDjhm6TBemuonOBxSUrEqUYcxKQR4BKi5pQ9OZBm
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode("123456");
        boolean matches = passwordEncoder.matches("123456", "$2a$10$NPvVw/DeZKSrLcDjhm6TBemuonOBxSUrEqUYcxKQR4BKi5pQ9OZBm");
        System.out.println(encode + "-> " + matches);

    }

}
