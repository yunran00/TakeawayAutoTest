package com.sky.takeaway.utils;

import com.sky.takeaway.config.TestConfig;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/*
购物车工具类
用于添加商品到购物车
 */
public class CartUtils extends TestConfig {

    /**
     * 默认测试使用购物车（菜品为54号，没有口味）
     */
    public static void addDefaultDish(String token){
        addDish(token,54L,null);
    }

    /**
     * 通用添加方法（添加套餐，套餐没有口味）
     */
    public static void addSetmeal(String token, Long setmealId){
        Map<String,Object> params = new HashMap<>();
        params.put("setmealId",setmealId);
        doAdd(token,params);
    }

    /**
     * 通用添加方法（添加菜品 + 口味）
     */
    public static void addDish(String token, Long dishId, String dishFlavor){
        Map<String,Object> params = new HashMap<>();
        params.put("dishId",dishId);
        if (dishFlavor != null && !dishFlavor.isEmpty()){
            params.put("dishFlavor",dishFlavor);
        }
        doAdd(token,params);
    }

    /**
     * 调用接口，加入购物车
     */
    public static void doAdd(String token,Map<String,Object> params){
        given()
                .log().all()
                .contentType("application/json")
                .header("authentication",token)
                .body(params)
        .when()
                .post(props.getProperty("user.cart.add"))
        .then()
                .log().all()
                .statusCode(200)
                .body("code",equalTo(1));

    }
}
