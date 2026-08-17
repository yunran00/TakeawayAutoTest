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

    public static void addCartDish(String token){

        Map<String,Object> params = new HashMap<>();
        params.put("dishId",51);
        params.put("setmealId",45);

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
