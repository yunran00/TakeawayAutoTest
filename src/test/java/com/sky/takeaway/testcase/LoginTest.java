package com.sky.takeaway.testcase;

import com.sky.takeaway.config.TestConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@Slf4j
public class LoginTest extends TestConfig {

    private final String loginPath = props.getProperty("login.path");

    /**
     * 正常场景
     */
    @Nested
    @DisplayName("正向场景")
    @Tag("smoke")
    class PositiveTests {
        @ParameterizedTest
        @DisplayName("TC-LOGIN-001: 正常登录成功")
        @CsvSource({"admin, 123456"})
        public void testLoginSuccess(String username, String password) {
            Map<String, Object> params = new HashMap<>();
            params.put("username", username);
            params.put("password", password);

            given()
                    .log().all()
                    .contentType("application/json")
                    .body(params)
                    .when()
                    .post(loginPath)
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("code", not(equalTo(0)))
                    .body("data.token", notNullValue());
        }
    }

    /**
     * 异常场景（参数化测试）
     * _null_：字段存在但为空（传空字符串）
     * _missing_：字段完全缺失（不 put 该字段）
     */

    @Nested
    @DisplayName("异常场景")
    @Tag("regression")
    class NegativeTests {
        @ParameterizedTest
        @DisplayName("TC-LOGIN-002~007: 登录异常场景")
        @CsvSource({
                "admin, 1234, 密码错误",
                "aaaa, 123456, 账号不存在",
                "admin, _null_ , 密码错误",
                "_null_, 123456, 账号不存在",
                "admin, _missing_, 密码不能为空",
                "_missing_, 123456, 账号不能为空"
        })
        public void testLoginWithInvalidData(String username, String password, String expectedMsg) {
            Map<String, Object> params = new HashMap<>();

            // 处理用户名
            if ("_missing_".equals(username)) {
                // 不 put username，字段缺失
            } else if ("_null_".equals(username)) {
                params.put("username", "");
            } else {
                params.put("username", username);
            }

            // 处理密码
            if ("_missing_".equals(password)) {
                // 不 put password，字段缺失
            } else if ("_null_".equals(password)) {
                params.put("password", "");
            } else {
                params.put("password", password);
            }

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
                    .body("msg", containsString(expectedMsg));
        }
    }
}
