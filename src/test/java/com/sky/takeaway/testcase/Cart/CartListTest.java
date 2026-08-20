package com.sky.takeaway.testcase.Cart;

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
                    .body("data",not(empty()))
                    //断言data集合不为空(RestAssured在解析 JSON 时，
                    // 只会生成 Java 集合框架中的类型（List、Map、String、Integer 等），
                    // 不会生成 Java 原生数组（Object[]、String[] 等）)
                    .body("data",hasSize(1));//断言data数组内有一个元素
        }

        @Test
        @DisplayName("TC-CART-LIST-002:购物车没有商品时查看")
        public void testListCartWithEmptyCart(){
            given()
                    .log().all()
                    .header("authentication",token)
                    .when()
                    .get(cartListPath)
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("code",equalTo(1))
                    .body("data",empty())
                    .body("data",hasSize(0));
        }
    }

    @Nested
    @DisplayName("查看购物车异常场景")
    @Tag("regression")
    class NegativeTests{

        @Test
        @DisplayName("TC-CART-LIST-003:没有登录时查看购物车")
        public  void testListCartWithoutToken(){

            given()
                    .log().all()
                    .when()
                    .get(cartListPath)
                    .then()
                    .log().all()
                    .statusCode(401);//返回401，无响应体
        }
    }
}
