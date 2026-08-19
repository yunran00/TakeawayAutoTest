package com.sky.takeaway.testcase;

import com.sky.takeaway.base.UserBaseTest;
import com.sky.takeaway.model.OrderTestData;
import com.sky.takeaway.utils.CartUtils;
import com.sky.takeaway.utils.DbUtils;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class OrderTest extends UserBaseTest {

    private final String submitPath = props.getProperty("order.path");

    @BeforeEach
    public void prepareCart() {
        DbUtils.clearCartByUserId(userId);
        CartUtils.addDefaultDish(token);
        log.info("购物车已准备");
    }

    @Nested
    @DisplayName("下单正向场景")
    @Tag("smoke")
    class PositiveTests {
        @ParameterizedTest
        @DisplayName("正常下单成功")
        @MethodSource("com.sky.takeaway.testcase.OrderTestDataProvider#positiveDataProvider")
        void testOrderSuccess(OrderTestData data) {
            Integer orderId = submitOrder(data);
            verifyOrderInDatabase(orderId,data.getAmount());
        }
    }

    @Nested
    @DisplayName("下单异常场景")
    @Tag("regression")
    class NegativeTests {
        @ParameterizedTest
        @DisplayName("异常下单 - 参数校验")
        @MethodSource("com.sky.takeaway.testcase.OrderTestDataProvider#negativeDataProvider")
        void testOrderFailure(OrderTestData data) {
            submitOrderAndExpectFailure(data);
        }
    }

    @Step("提交订单")
    private Integer submitOrder(OrderTestData data) {
        Map<String, Object> params = buildParams(data);

        return given()
                .log().all()
                .contentType("application/json")
                .header("authentication", token)
                .body(params)
                .when()
                .post(submitPath)
                .then()
                .log().all()
                .statusCode(200)
                .body("code", equalTo(data.getExpectedCode()))
                .body("data.orderAmount", data.getExpectedCode() == 1 ? notNullValue() : nullValue())
                .extract()
                .path("data.id");
    }

    @Step("验证订单数据落库")
    private void verifyOrderInDatabase(Integer orderId, Double amount) {
        Double dbAmount = DbUtils.getOrderAmount(orderId.longValue());
        assertNotNull(dbAmount, "数据库订单金额不应为空");
        assertEquals(amount, dbAmount, 0.01, "金额不一致");

        Integer dbStatus = DbUtils.getOrderStatus(orderId.longValue());
        assertNotNull(dbStatus, "状态不应为空");
        assertEquals(1, dbStatus, "状态异常");

        log.info("数据库校验通过: 金额={}, 状态={}", dbAmount, dbStatus);
    }

    private void submitOrderAndExpectFailure(OrderTestData data) {
        Map<String, Object> params = buildParams(data);

        given()
                .log().all()
                .contentType("application/json")
                .header("authentication", token)
                .body(params)
                .when()
                .post(submitPath)
                .then()
                .log().all()
                .statusCode(200)
                .body("code", equalTo(data.getExpectedCode()));
    }

    private Map<String, Object> buildParams(OrderTestData data) {
        Map<String, Object> params = new HashMap<>();
        params.put("addressBookId", data.getAddressBookId());
        params.put("amount", data.getAmount());
        params.put("deliveryStatus", data.getDeliveryStatus());
        params.put("estimatedDeliveryTime", data.getEstimatedDeliveryTime());
        params.put("packAmount", data.getPackAmount());
        params.put("payMethod", data.getPayMethod());
        params.put("remark", data.getRemark());
        params.put("tablewareNumber", data.getTablewareNumber());
        params.put("tablewareStatus", data.getTablewareStatus());
        return params;
    }
}