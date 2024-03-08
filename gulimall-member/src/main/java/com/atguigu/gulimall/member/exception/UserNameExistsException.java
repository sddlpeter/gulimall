package com.atguigu.gulimall.member.exception;

public class UserNameExistsException extends RuntimeException{
    public UserNameExistsException() {
        super("用户名已存在");
    }
}
