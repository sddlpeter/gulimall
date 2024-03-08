package com.atguigu.gulimall.thirdparty.component;


import com.atguigu.gulimall.thirdparty.util.HttpUtils;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
@Data
@Component
public class SmsComponent {

    private String host;
    private String path;
    private String templateId;
    private String signId;
    private String appcode;

    public void sendCode(String phone, String code) {
        String method = "POST";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);
        querys.put("templateId", templateId);
        querys.put("smsSignId", signId);
        querys.put("param", "**code**:"+code+",**minute**:5");
        Map<String, String> bodys = new HashMap<String, String>();

        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
