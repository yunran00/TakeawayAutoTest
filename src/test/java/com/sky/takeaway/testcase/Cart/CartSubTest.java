package com.sky.takeaway.testcase.Cart;

import com.sky.takeaway.base.UserBaseTest;
import com.sky.takeaway.utils.CartUtils;
import com.sky.takeaway.utils.DbUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;


@Slf4j
public class CartSubTest extends UserBaseTest {

    private final String cartSubPath = props.getProperty("user.cart.sub");
    private static final Long TEST_SETMEAL_ID = 48L;
    private static final Long TEST_DISH_ID = 71L;

    @BeforeEach
    public void cart() {
        DbUtils.clearCartByUserId(userId);
        log.info("清空购物车");
        CartUtils.addSetmeal(token,TEST_SETMEAL_ID);
        CartUtils.addDish(token,TEST_DISH_ID,null);
        log.info("购物车数据已准备");
    }

    @Nested
    @DisplayName("删除购物车中一个商品正向场景")
    @Tag("smoke")
    class PositiveTests{

        @Test
        @DisplayName("TC-CART-SUB-001:删除菜品成功（购物车中该菜品数量为1）")
        public void testDeleteDishSuccess(){

            Map<String,Object> params = new HashMap<>();
            params.put("dishId",TEST_DISH_ID);

            Double total = DbUtils.getCartTotalByUserId(userId);
            Double price = DbUtils.getPriceByDishId(TEST_DISH_ID);
            log.info("删除前购物车金额为：{}，删除的物品的金额为：{}",total,price);

            given()
                    .log().all()
                    .contentType("application/json")
                    .header("authentication",token)
                    .body(params)
                    .when()
                    .post(cartSubPath)
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("code",equalTo(1));

            Double total1 = DbUtils.getCartTotalByUserId(userId);
            log.info("删除后购物车金额为：{}",total);
            assertEquals(total - price, total1,"删除后购物车金额与期望不符");

        }


        @Test
        @DisplayName("TC-CART-SUB-002:删除套餐成功")
        public void testDeleteSetmealSuccess(){
            Map<String,Object> params = new HashMap<>();
            params.put("setmealId",TEST_SETMEAL_ID);

            Double total = DbUtils.getCartTotalByUserId(userId);
            Double price = DbUtils.getPriceBySetmealId(TEST_SETMEAL_ID);
            log.info("删除前购物车金额为：{}，删除的物品的金额为：{}",total,price);

            given()
                    .log().all()
                    .contentType("application/json")
                    .header("authentication",token)
                    .body(params)
                    .when()
                    .post(cartSubPath)
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("code",equalTo(1));

            Double total1 = DbUtils.getCartTotalByUserId(userId);
            log.info("删除后购物车金额为：{}",total);
            assertEquals(total - price, total1,"删除后购物车金额与期望不符");

        }
    }

    @Nested
    @DisplayName("删除购物车中一个商品异常场景")
    @Tag("regression")
    class NegativeTests{

        @Test
        @DisplayName("TC-CART-SUB-003:删除不存在的商品，返回业务错误")
        public void testDeleteNonExistentDish(){
            Map<String,Object> params = new HashMap<>();
            params.put("dishId",999999L);

            given()
                    .log().all()
                    .contentType("application/json")
                    .header("authentication",token)
                    .body(params)
                    .when()
                    .post(cartSubPath)
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("code",equalTo(0))
                    .body("msg",containsString("商品不存在无法删除"));
        }

        @Test
        @DisplayName("TC-CART-DEL-004: 不传任何ID，返回业务错误")
        public void testDeleteWithoutAnyId() {
            Map<String,Object> params = new HashMap<>();

            given()
                    .log().all()
                    .contentType("application/json")
                    .header("authentication",token)
                    .body(params)
                    .when()
                    .post(cartSubPath)
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("code",equalTo(0))
                    .body("msg",containsString("请选择要删除的商品"));

        }

        @Test
        @DisplayName("TC-CART-DEL-005: 未登录删除，返回401")
        public void testDeleteWithoutToken() {
            Map<String,Object> params = new HashMap<>();
            params.put("dishId",TEST_DISH_ID);

            given()
                    .log().all()
                    .contentType("application/json")
                    .body(params)
                    .when()
                    .post(cartSubPath)
                    .then()
                    .log().all()
                    .statusCode(401);
        }
    }
}
