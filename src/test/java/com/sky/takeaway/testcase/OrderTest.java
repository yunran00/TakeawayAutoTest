package com.sky.takeaway.testcase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.takeaway.config.TestConfig;
import com.sky.takeaway.model.OrderTestData;
import com.sky.takeaway.utils.CartUtils;
import com.sky.takeaway.utils.DbUtils;
import com.sky.takeaway.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class OrderTest extends TestConfig {

    private String token;
    private Long userId = 4L;
    private final String submitPath = props.getProperty("order.path");
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        token = TokenUtils.getUserToken();
        log.info("获取到用户 token，用户ID: {}", userId);

        // 清空购物车，保证测试环境干净
        DbUtils.clearCartByUserId(userId);
        // 添加购物车
        CartUtils.addCartDish(token);
        log.info("购物车已准备");
    }

    /**
     * 从 JSON 文件读取测试数据
     */
    static Stream<Arguments> orderDataProvider() throws Exception {
        File jsonFile = new File("src/test/resources/data/order_test_data.json");
        OrderTestData[] dataArray = objectMapper.readValue(jsonFile, OrderTestData[].class);

        return Stream.of(dataArray)
                .map(data -> Arguments.of(
                        data.getAddressBookId(),
                        data.getAmount(),
                        data.getDeliveryStatus(),
                        data.getEstimatedDeliveryTime(),
                        data.getPackAmount(),
                        data.getPayMethod(),
                        data.getRemark(),
                        data.getTablewareNumber(),
                        data.getTablewareStatus(),
                        data.getExpectedCode()
                ));
    }

    @ParameterizedTest
    @DisplayName("下单接口参数化测试")
    @MethodSource("orderDataProvider")
    public void testOrderSubmit(
            Integer addressBookId,
            Double amount,
            Integer deliveryStatus,
            String estimatedDeliveryTime,
            Integer packAmount,
            Integer payMethod,
            String remark,
            Integer tablewareNumber,
            Integer tablewareStatus,
            Integer expectedCode) {

        log.info("执行下单测试用例 - 期望业务码: {}", expectedCode);

        // 1. 构造请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("addressBookId", addressBookId);
        params.put("amount", amount);
        params.put("deliveryStatus", deliveryStatus);
        params.put("estimatedDeliveryTime", estimatedDeliveryTime);
        params.put("packAmount", packAmount);
        params.put("payMethod", payMethod);
        params.put("remark", remark);
        params.put("tablewareNumber", tablewareNumber);
        params.put("tablewareStatus", tablewareStatus);

        // 2. 调用下单接口
        Integer orderId = given()
                .log().all()
                .contentType("application/json")
                .header("authentication", token)
                .body(params)
                .when()
                .post(submitPath)
                .then()
                .log().all()
                .statusCode(200)
                .body("code", equalTo(expectedCode))
                .body("data.orderAmount", expectedCode == 1 ? notNullValue() : nullValue())
                .extract()
                .path("data.id");

        log.info("下单完成 - 订单ID: {}, 业务码: {}", orderId, expectedCode);

        // 3. 只有期望成功时才执行数据库校验
        if (expectedCode == 1 && orderId != null) {
            // 校验订单金额
            Double dbAmount = DbUtils.getOrderAmount(orderId.longValue());
            assertNotNull(dbAmount, "数据库中订单金额不应为空");
            assertEquals(amount.floatValue(), dbAmount, 0.01, "数据库订单金额与预期不一致");

            // 校验订单状态
            Integer dbStatus = DbUtils.getOrderStatus(orderId.longValue());
            assertNotNull(dbStatus, "数据库中订单状态不应为空");
            assertEquals(1, dbStatus, "订单状态异常");

            // 校验订单是否存在
            boolean exists = DbUtils.orderExists(orderId.longValue());
            assertTrue(exists, "订单不存在");

            log.info("数据库校验通过 - 金额: {}, 状态: {}", dbAmount, dbStatus);
        } else {
            log.info("异常场景校验完成，不执行数据库校验");
        }
    }
}