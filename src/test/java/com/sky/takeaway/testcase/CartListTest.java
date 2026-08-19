package com.sky.takeaway.testcase;

import com.sky.takeaway.base.UserBaseTest;
import com.sky.takeaway.utils.CartUtils;
import com.sky.takeaway.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@Slf4j
public class CartListTest extends UserBaseTest {

    private final String cartListPath = props.getProperty("user.cart.list");

    @BeforeEach
    public void cleanCart(){
        DbUtils.clearCartByUserId(userId);
        log.info("已清空购物车");
    }

    @Nested
    @DisplayName("查看购物车正向场景")
    @Tag("smoke")
    class PositiveTests {

        @Test
        @DisplayName("TC-CART-LIST-001:查看成功且购物车有商品")
        public void testListCartWithItems(){

            CartUtils.addDefaultDish(token);
            log.info("已向购物车内添加商品");

            given()
                    .log().all()
                    .header("authentication",token)
                    .when()
                    .get(cartListPath)
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("code",equalTo(1))
                    .body("data",not(emptyArray()))//断言data数组不为空
                    .body("data",hasSize(1));//断言data数组内有一个元素
        }
    }
}
