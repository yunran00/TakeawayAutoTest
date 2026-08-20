package com.sky.takeaway.testcase.Cart;

import com.sky.takeaway.base.UserBaseTest;
import com.sky.takeaway.utils.CartUtils;
import com.sky.takeaway.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class CartCleanTest extends UserBaseTest {

    private final String cartCleanPath = props.getProperty("user.cart.clean");

    @BeforeEach
    public void prepareCart(){
        DbUtils.clearCartByUserId(userId);
        CartUtils.addDefaultDish(token);
        log.info("购物车数据已准备");
    }

    @Nested
    @DisplayName("清空购物车正常场景")
    @Tag("smoke")
    class PositiveTests{

        @Test
        @DisplayName("TC-CART-CLEAN-001:正常清空购物车")
        public void testCleanCartSuccess(){
            given()
                    .log().all()
                    .header("authentication",token)
                    .when()
                    .delete(cartCleanPath)
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("code",equalTo(1))
                    .body("data",nullValue())
                    .body("msg",nullValue());

            //数据验证
            Double amount = DbUtils.getOrderAmount(userId);
            assertEquals(0.0, amount, "清空购物车后购物车金额应为零元");
            log.info("数据库校验已完成，购物车金额为：{}",amount);
        }

        @Test
        @DisplayName("TC-CART-CLEAN-002:购物车为空时清空购物车")
        public void testCleanCartTwice(){
            DbUtils.clearCartByUserId(userId);

            given()
                    .log().all()
                    .header("authentication",token)
                    .when()
                    .delete(cartCleanPath)
                    .then()
                    .statusCode(200)
                    .body("code",equalTo(1))
                    .body("data",nullValue())
                    .body("msg",nullValue());

            //数据库校验
            Double amount = DbUtils.getOrderAmount(userId);
            assertEquals(0.0, amount,"清空购物车后购物车金额应为零元");
            log.info("购物车为空时清空购物车校验通过");
        }
    }

    @Nested
    @DisplayName("清空购物车异常场景")
    @Tag("regression")
    class NegativeTests{

        @Test
        @DisplayName("TC-CART-CLEAN-003:不登录清空购物车")
        public void testCleanCartWithoutToken(){

            given()
                    .log().all()
                    .when()
                    .delete(cartCleanPath)
                    .then()
                    .log().all()
                    .statusCode(401);
        }
    }
}
