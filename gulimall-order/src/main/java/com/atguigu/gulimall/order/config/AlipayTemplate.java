package com.atguigu.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "9021000136631165";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDtXg9eE8AwW5Ewo4gfuU3G9SDl/U9Kxq1T1IIZ88JoC1H/ZCkKwvpwYuf3894GCJI3sX0K9TyNIu1OqgzhnJVYOS7I/DVgWjKYwYsJRd9zLS7R2W8h5wDkrsbHql7NCW2GtcRaLb8jUy8Bm5f3gqKVhkZ1EvaXTNowtdSJezARnK7lHCpKO5hwNRwyOI4lP8RYcHmHRCNZ1v1cUUdPS+KS6PpODjuyDHjvAkmemAWwGXIozyFWm25PNFAsmxJRfVcrN0LULxWp00MwXze9y6G8+MgSQg/9wWmn09IAzueVq0OALC9Q63gtwoIQv4EIWJOErsFZal0/bVlVZASyP5eJAgMBAAECggEAPy/420xarwTRI6eExx0nDT2X4mGKSBeXcGdzssDnEZdh3qClJE4/KMfLhIAQrH5/nf/Lj600z0Vq8rPl9fEe+HtQbwRkmR+ptGxhPLizARAYFJjTMq0x2a6FEAXUcrR9yYx9lpvy885jBwSKccL+0NsLyoKfzisRMkJVL7vxUd5mmYeFSG+A2V3+hqjNXL9m+6btVpCbAPgU+etySMgoWGn2oS85CTGcxkvscU/2awOMh4BytTZjUL0AOWVWLwnht71lsQ26G87+nju9zxgTNQMkbsKwEgl//wnv9rwD8FDnEMgixkqMuYeFEOTqIMyyYSGZTOwKj8YocV6Jo0kwYQKBgQD4DU3hlR5br+86qJZaHwJVHxINlqi5IXjgSqdQDERFcN/CiooWqspqqhPDhc+jitv5Og7HAFAfaAxS9+/Lcznz3bSAjbv2OnmASEHGC2hTeGHai5J8jGNCRgUP3ETEkim3R5ngprUNTEsyWfjl8YIMk9husu84QgxxFg/4m0JHjQKBgQD0+R0ZmuUqErpGivbP9I+6uGMSyfyA4gLBRPUfUW7XrFjhq4qRjM9WBpZDsbUqIr79kZ3lRiqMZjKg0DfjSTSOZAiQOxJCIXmY6+VFojgSeIAiAFDMzpUXnUvh25TeP0ZkUA+3bg9IOE1k5Z1yAbIPWfYDFMAAdF93S4XG3HVC7QKBgDkANalk7vj60OqV2xcSkSKmZSuiN1tziadhAFmwt++NJJ4738edr64bLzd7BF/8hNgcs1Cfjcnpyws11nDWJbcYd2YjK9JFVa5Y7GRqMqsbusDR2fOs/xI3rOocH+FcBSIM3ht1s50+rvj/G+cbPFtPjijCDZ7L7rf97eWfJ/fxAoGBAPOiihF8HqxgYchkzqD17rhaR5ZZMnqPzQOgbFUQbTesXd1a+P5W16IL/QgoRjA4zFBtsWZkCU8HGRUc4Gh41v9Cd83SIg+JMWEoQyak9jVQiMbIH5QBZQBOXTisZPtAXObJRUg9+o7uB/Lv9k17aDaiVuyrh2UsWAHSJ6MmgKzlAoGAc96nHKOpNB/rIrmZfEKgaUfjW+I7eDssQbllaZArTujdLoBGEr8jMunaie75wTkHIYDYDcq5ACzd7755wf0G3DA3rOfXGcER9fnyyNI7M5/vi6neDRFBK/lZEu2gNA2KCYP+zIYiZp3uAyRhISIqT9R8X80EY6v9lq8LdWkkZ9Q=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzVbOycS7AjruGWzXTgnNXU08mbMu+TSmGDm2w9xdmFEqbA3F1YZ3tL81FHd35F5Z1U6qzWmauWynu0gtrwwfLkzeQxBWbGRZErhURUa4f/Q01t/i2z+p4WVG5ACFMyUxyTuScKIAYO2/DKEiEqaM4yZzTnYlZw+UJpBxpyWc1IV8WKptjnFp1RPXA+WPfo9MvbX9ArCqQsyj+8uysAAkKQnAlcw3P2dWXhB5dAod7HPgQ0Q/qkLTPSloaSAD2FoKtlT2b0lrn820m+YOj1daQU9JR6FAJ/BwW/2x346v2psAOAOiD3bToWXuvHXlbMSjluH7VVxHSzLm8WJ6OQfpBQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    // TODO 需要用内网穿透的地址， https://8455-76-147-228-41.ngrok-free.app -> http://order.gulimall.com:80
    private  String notify_url = "https://8455-76-147-228-41.ngrok-free.app/paid/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url = "http://member.gulimall.com/memberOrder.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    private String timeout = "30m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+timeout+"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
