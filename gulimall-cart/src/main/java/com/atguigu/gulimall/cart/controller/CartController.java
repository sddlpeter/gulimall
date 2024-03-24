package com.atguigu.gulimall.cart.controller;


import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.service.impl.CartServiceImpl;
import com.atguigu.gulimall.cart.vo.Cart;
import com.atguigu.gulimall.cart.vo.CartItem;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {

        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        cartService.changeItemCount(skuId, num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }


    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);

        return "redirect:http://cart.gulimall.com/cart.html";

    }

    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {
        // 快速得到用户信息
        // ThreadLocal 同一个线程共享数据, id, user-key
//        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
//        System.out.println(userInfoTo);

        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);

        return "cartList";
    }


    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num,
                            RedirectAttributes ra)
            throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId, num);
        ra.addAttribute("skuId", skuId);
        // model.addAttribute("skuId", skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess.html";
    }

    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {

        CartItem item = cartService.getCartItem(skuId);
        model.addAttribute("item", item);

        return "success";

    }
}
