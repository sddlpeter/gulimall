package com.atguigu.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.atguigu.gulimall.thirdparty.component.SmsComponent;
import com.atguigu.gulimall.thirdparty.util.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class GulimallThirdPartyApplicationTests {

	@Test
	void contextLoads() {
	}

    @Autowired
    OSSClient ossClient;

    @Autowired
    SmsComponent smsComponent;

    @Test
    public void testSendCode() {
        smsComponent.sendCode("13609281960", "6789");
    }


    @Test
    public void sendSms() {

        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";
        String appcode = "1f79478feba44d768a8df63861ff541e";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", "13609281960");
        querys.put("param", "**code**:6789,**minute**:5");

//smsSignId（短信前缀）和templateId（短信模板），可登录国阳云控制台自助申请。参考文档：http://help.guoyangyun.com/Problem/Qm.html

        querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
        querys.put("templateId", "908e94ccf08b4476ba6c876d13f084ad");
        Map<String, String> bodys = new HashMap<String, String>();


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从\r\n\t    \t* https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java\r\n\t    \t* 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpload() throws FileNotFoundException {

        // String endpoint = "oss-us-west-1.aliyuncs.com";
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        //EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

        // String accessKeyId = "LTAI5tDszjeKJt6L7qhnSVSZ";
        // String accessKeySecret = "2d6trliehgKdh1JQOiTrE6DAKewsGE";

        String bucketName = "gulimall-seattle";
        String objectName = "hahaha.jpg";
        String filePath= "C:\\Users\\Admin\\Downloads\\pic.jpg";

        // 创建OSSClient实例。
        // OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);
        // OSS ossClient = new OSSClientBuilder().build(endpoint,accessKeyId, accessKeySecret);

        try {
            InputStream inputStream = new FileInputStream(filePath);
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            // 创建PutObject请求。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

}
