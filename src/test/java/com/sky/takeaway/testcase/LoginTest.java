package com.sky.takeaway.testcase;

import com.sky.takeaway.config.TestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class LoginTest extends TestConfig {

    private final String loginPath = props.getProperty("login.path");

    @Test
    @DisplayName("TC-LOGIN-002:密码错误")
    public void testEmployeeLogin_02(){

        Map<String,Object> params = new HashMap();
        params.put("username", props.getProperty("test.username"));
        params.put("password", props.getProperty("test.wrong.password"));//正确是123456

        given()
                .log().all()
                .contentType("application/json")
                .body(params)
        .when()
                .post(loginPath)
        .then()
                .log().all()
                .statusCode(200)
                .body("code", equalTo(0))
                .body("msg",containsString("密码错误"));
    }

    @Test
    @DisplayName("TC-LOGIN-003:用户名错误")
    public void testEmployeeLogin_03(){

        Map<String,Object> params = new HashMap();
        params.put("username", props.getProperty("test.wrong.username"));//正确的是admin
        params.put("password", props.getProperty("test.password"));

        given()
                .log().all()
                .contentType("application/json")
                .body(params)
        .when()
                .post(loginPath)
        .then()
                .log().all()
                .statusCode(200)
                .body("code", equalTo(0))
                .body("msg",containsString("账号不存在"));
    }

    @Test
    @DisplayName("TC-LOGIN-004:密码为空(当前服务有bug，返回500)")
    public void testEmployeeLogin_04(){

        Map<String,Object> params = new HashMap();
        params.put("username",  props.getProperty("test.username"));

        given()
                .log().all()
                .contentType("application/json")
                .body(params)
        .when()
                .post(loginPath)
        .then()
                .log().all()
                .statusCode(200)
                .body("code",equalTo(0))
                .body("msg",containsString("密码不能为空"));
    }

    @Test
    @DisplayName("TC-LOGIN-005:用户名为空")
    public void testEmployeeLogin_05(){

        Map<String,Object> params = new HashMap();
        params.put("password", props.getProperty("test.password"));

        given()
                .log().all()
                .contentType("application/json")
                .body(params)
        .when()
                .post(loginPath)
        .then()
                .log().all()
                .statusCode(200)
                .body("code",equalTo(0))
                .body("msg",containsString("账号不存在"));
    }
}
