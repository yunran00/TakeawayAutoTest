package com.sky.takeaway.testcase;

import com.sky.takeaway.config.TestConfig;
import com.sky.takeaway.utils.CartUtils;
import com.sky.takeaway.utils.DbUtils;
import com.sky.takeaway.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class OrderTest extends TestConfig {

    private String token;
    private Long id = 4L;
    private final String submitPath = props.getProperty("order.path");

    @BeforeEach
    public void login() {
        token = TokenUtils.getUserToken();
        log.info("获取用户的token和用户的id：{}，{}", token, id);

        DbUtils.clearCartByUserId(id);
        CartUtils.addCartDish(token);
        log.info("购物车已经准备");


    }

    @Test
    @DisplayName("TC-SUBMIT-001:下单成功")
    public void testOrderSubmit(){

        Double cartTotal = DbUtils.getCartTotalByUserId(id);

        Map<String,Object> params = new HashMap<>();
        params.put("addressBookId",props.getProperty("order.addressBookId"));
        params.put("amount", cartTotal);
        params.put("deliveryStatus",props.getProperty("order.deliveryStatus"));
        params.put("estimatedDeliveryTime",props.getProperty("order.estimatedDeliveryTime"));
        params.put("packAmount",props.getProperty("order.packAmount"));
        params.put("payMethod",props.getProperty("order.payMethod"));
        params.put("remark",props.getProperty("order.remark"));
        params.put("tablewareNumber",props.getProperty("order.tablewareNumber"));
        params.put("tablewareStatus",props.getProperty("order.tablewareStatus"));

        Integer id = given()
                .log().all()
                .contentType("application/json")
                .header("authentication", token)
                .body(params)
                .when()
                .post(submitPath)
                .then()
                .log().all()
                .statusCode(200)
                .body("code", equalTo(1))
                .body("data.orderAmount", equalTo(cartTotal.floatValue()))
                .body("msg", nullValue())
                .extract()
                .path("data.id");

        log.info("下单成功，订单id为：{}",id);

        // 校验订单金额是否落库正确
        Double dbAmount = DbUtils.getOrderAmount(id.longValue());
        assertNotNull(dbAmount, "数据库中订单金额不应为空");
        assertEquals(cartTotal.floatValue(), dbAmount,"数据库订单金额与预期不一致");
        // 校验订单状态是否落库正确
        Integer dbStatus = DbUtils.getOrderStatus(id.longValue());
        assertNotNull(dbStatus, "数据库中订单状态不应为空");
        assertEquals(1, dbStatus, "数据库订单状态异常");
        //  校验订单是否存在
        boolean exists = DbUtils.orderExists(id.longValue());
        assertTrue(exists, "订单在数据库中不存在");

        log.info("数据库校验通过: 金额={}, 状态={}", dbAmount, dbStatus);
    }
}
