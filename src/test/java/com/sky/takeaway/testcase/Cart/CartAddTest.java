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
public class CartAddTest extends UserBaseTest {

    private final String cartAddPath = props.getProperty("user.cart.add");
    private static final Long TEST_DISH_ID = 66L;
    private static final Long TEST_SETMEAL_ID = 46L;
    private static final String TEST_DISH_FLAVOR = "微辣";

    @BeforeEach
    public void cleanCart() {
        DbUtils.clearCartByUserId(userId);
        log.info("购物车已清空");
    }

    @Nested
    @DisplayName("添加购物车正向场景")
    @Tag("smoke")
    class PositiveTests {

        @Test
        @DisplayName("TC-CART-ADD-001:添加菜品到购物车成功")
        public void testAddDishSuccess(){

            Map<String, Object> params = new HashMap<>();
            params.put("dishId",TEST_DISH_ID);

            given()
                    .log().all()
                    .contentType("application/json")
                    .header("authentication", token)
                    .body(params)
                    .when()
                    .post(cartAddPath)
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("code",equalTo(1));

            //数据库校验
            Double total = DbUtils.getCartTotalByUserId(userId);
            Double price = DbUtils.getPriceByDishId(TEST_DISH_ID);
            assertNotNull(price, "菜品价格不应为 null，请确认 dishId=" + TEST_DISH_ID + " 存在");
            assertNotNull(total, "购物车总金额不应为 null");
            assertEquals(price,total,"购物车实际金额不正确");
            log.info("数据校验通过添加的菜品id为：{}，菜品的单价为：{}",TEST_DISH_ID,price);
        }


        @Test
        @DisplayName("TC-CART-ADD-002:添加套餐到购物车成功")
        public void testAddSetmealSuccess(){
            Map<String, Object> params = new HashMap<>();
            params.put("setmealId",TEST_SETMEAL_ID);

            given()
                    .log().all()
                    .contentType("application/json")
                    .header("authentication", token)
                    .body(params)
                    .when()
                    .post(cartAddPath)
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("code",equalTo(1));

            Double total = DbUtils.getCartTotalByUserId(userId);
            Double price = DbUtils.getPriceBySetmealId(TEST_SETMEAL_ID);
            assertNotNull(total,"购物车总金额不应为 null");
            assertNotNull(price,"菜品价格不应为 null，请确认 dishId=" + TEST_SETMEAL_ID + " 存在");
            assertEquals(price,total,"购物车实际金额不正确");
            log.info("数据校验通过添加的套餐id为：{}，套餐的单价为：{}",TEST_SETMEAL_ID,price);
        }


        @Test
        @DisplayName("TC-CART-ADD-003:添加菜品和口味到购物车成功")
        public void testAddDishWithFlavor(){
            Map<String, Object> params = new HashMap<>();
            params.put("dishId",TEST_DISH_ID);
            params.put("dishFlavor",TEST_DISH_FLAVOR);

            given()
                    .log().all()
                    .contentType("application/json")
                    .header("authentication", token)
                    .body(params)
                    .when()
                    .post(cartAddPath)
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("code",equalTo(1));

            Double total = DbUtils.getCartTotalByUserId(userId);
            Double price = DbUtils.getPriceByDishId(TEST_DISH_ID);
            String flavor = DbUtils.getFlavorByUserId(userId);
            assertNotNull(price, "菜品价格不应为 null，请确认 dishId=" + TEST_DISH_ID + " 存在");
            assertNotNull(total, "购物车总金额不应为 null");
            assertNotNull(flavor,"菜品的口味不因为 null");
            assertEquals(price,total,"购物车实际金额不正确");
            log.info("数据校验通过添加的菜品id为：{}，菜品的单价为：{}，菜品的口味为：{}",TEST_DISH_ID,price,flavor);
        }

        @Test
        @DisplayName("TC-CART-ADD-004:重复添加同一商品，数量累加")
        public void testAddSetmealWithFlavor(){
            Map<String, Object> params = new HashMap<>();
            params.put("setmealId",TEST_SETMEAL_ID);

            //添加套餐
            CartUtils.addSetmeal(token,TEST_SETMEAL_ID);
            log.info("已添加一份套餐，套餐id为：{}，套餐数量为：{}",TEST_SETMEAL_ID,1);

            //再次添加套餐
            given()
                    .log().all()
                    .contentType("application/json")
                    .header("authentication", token)
                    .body(params)
                    .when()
                    .post(cartAddPath)
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("code",equalTo(1));
            log.info("添加同一份套餐，套餐id为：{}，套餐理论数量因该为：{}",TEST_SETMEAL_ID,2);

            //数据库校验
            Double total = DbUtils.getCartTotalByUserId(userId);
            Double price = DbUtils.getPriceBySetmealId(TEST_SETMEAL_ID);
            assertEquals(2 * price,total,"数据库实际金额与理论金额不符");
        }
    }

    @Nested
    @DisplayName("添加购物车异常场景")
    @Tag("regression")
    class NegativeTests{

        @Test
        @DisplayName("TC-CART-ADD-005:未登录添加购物车，返回401")
        public void testAddCartWithoutDishIdOrSetmealId(){
            Map<String, Object> params = new HashMap<>();
            params.put("dishId",TEST_DISH_ID);

            given()
                    .log().all()
                    .contentType("application/json")
                    .body(params)
                    .when()
                    .post(cartAddPath)
                    .then()
                    .log().all()
                    .statusCode(401);
        }

        @Test
        @DisplayName("TC-CART-ADD-006:不传任何商品ID，返回业务错误")
        public  void testAddCartWithDishId(){
            Map<String, Object> params = new HashMap<>();

            given()
                    .log().all()
                    .contentType("application/json")
                    .header("authentication", token)
                    .body(params)
                    .when()
                    .post(cartAddPath)
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("code",equalTo(0))
                    .body("msg",containsString("商品信息不能为空"));

        }

        @Test
        @DisplayName("C-CART-ADD-007:同时传入套餐id和菜品id，预期添加失败")
        public void testAddCartWithBothIds(){

            Map<String, Object> params = new HashMap<>();
            params.put("dishId",TEST_DISH_ID);
            params.put("setmealId",TEST_SETMEAL_ID);

            given()
                    .log().all()
                    .contentType("application/json")
                    .header("authentication", token)
                    .body(params)
                    .when()
                    .post(cartAddPath)
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("code",equalTo(0))
                    .body("msg",containsString("不能同时添加菜品和套餐"));
        }

        @Test
        @DisplayName("C-CART-ADD-008:传入不存在的 dishId，返回业务错误")
        public void testAddCartWithNonExistentDish(){
            Map<String, Object> params = new HashMap<>();
            params.put("dishId",0);

            given()
                    .log().all()
                    .contentType("application/json")
                    .header("authentication", token)
                    .body(params)
                    .when()
                    .post(cartAddPath)
                    .then()
                    .log().all()
                    .statusCode(200)
                    .body("code",equalTo(0))
                    .body("msg",containsString("菜品不存在"));
        }
    }
}
