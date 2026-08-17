package com.sky.takeaway.utils;
import com.sky.takeaway.config.TestConfig;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/*
Token 工具类
负责登录并返回 token，供所有测试类复用
 */
public class TokenUtils extends TestConfig {

    /**
     * 员工登录
     * @return
     */
    public static String getToken(){

        Map<String, Object> params = new HashMap<>();
        params.put("username", props.getProperty("test.username"));
        params.put("password", props.getProperty("test.password"));

        // 发送登录请求到测试接口
        return
                given()
                        .log().all()
                        .contentType("application/json")
                        .body(params)
                .when()
                        .post(props.getProperty("login.path"))
                .then()
                        .log().all()
                        .statusCode(200)
                        .body("code", equalTo(1))
                        .body("data.token", notNullValue())
                .extract()
                        .path("data.token");

    }

    /**
     * 用户登录（C端）
     * 使用测试专用 code（mock_code），跳过微信登录
     */
    public static String getUserToken() {
        String userLoginPath = props.getProperty("user.login.path");
        String mockCode = props.getProperty("user.mock.code");

        Map<String, Object> params = new HashMap<>();
        params.put("code", mockCode);

        return given()
                .contentType("application/json")
                .body(params)
                .when()
                .post(userLoginPath)
                .then()
                .statusCode(200)
                .body("code", equalTo(1))
                .body("data.token", notNullValue())
                .extract()
                .path("data.token");
    }
}
