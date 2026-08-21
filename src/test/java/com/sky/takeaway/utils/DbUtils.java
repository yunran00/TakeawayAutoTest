package com.sky.takeaway.utils;

import com.sky.takeaway.config.TestConfig;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

/**
 * 数据库工具类
 * 负责数据库查询、校验等操作
 * 所有配置从 application.properties 读取
 */
@Slf4j
public class DbUtils extends TestConfig {

    private static final String JDBC_URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    static {
        String host = props.getProperty("db.host");
        String port = props.getProperty("db.port");
        String database = props.getProperty("db.database");
        JDBC_URL = String.format(
                "jdbc:mysql://%s:%s/%s?" +
                        "serverTimezone=Asia/Shanghai" +
                        "&useUnicode=true&characterEncoding=utf-8" +
                        "&useSSL=false",
                host, port, database
        );
        USERNAME = props.getProperty("db.username");
        PASSWORD = props.getProperty("db.password");
        log.info("数据库连接初始化完成");
    }

    /**
     * 查询单条记录，返回指定字段的值
     */
    public static Object queryOne(String sql, String columnName, Object... params) {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject(columnName);
                }
            }
        } catch (SQLException e) {
            log.error("数据库查询失败, SQL: {}, 参数: {}", sql, params, e);
            throw new RuntimeException("数据库查询失败：" + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 查询订单金额
     */
    public static Double getOrderAmount(Long orderId) {
        Object result = queryOne("select amount from orders where id = ?", "amount", orderId);
        if (result == null) {
            return 0.0;
        }
        return ((Number) result).doubleValue();
    }

    /**
     * 查询订单状态
     */
    public static Integer getOrderStatus(Long orderId) {
        Object result = queryOne("select status from orders where id = ?", "status", orderId);
        if (result == null) {
            return null;
        }
        return (Integer) result;
    }

    /**
     * 判断订单是否存在
     */
    public static boolean orderExists(Long orderId) {
        Object result = queryOne("select id from orders where id = ?", "id", orderId);
        return result != null;
    }

    /**
     * 根据用户的id查询购物车总金额
     */
    public static double getCartTotalByUserId(Long userId) {
        Object result = queryOne("select sum(amount) from shopping_cart where user_id = ?", "sum(amount)", userId);
        return result == null ? 0.0 : ((Number) result).doubleValue();
    }

    /**
     * 根据用户id查询购物车中菜品的口味
     */
    public static String getFlavorByUserId(Long userId) {
        Object result = queryOne(
                "select dish_flavor from shopping_cart where user_id = ?", "dish_flavor", userId);
        return result == null ? null : result.toString();
    }

    /**
     * 根据菜品的id查询菜品金额
     */
    public static Double getPriceByDishId(Long orderId) {
        Object result = queryOne("select price from dish where id = ?", "price", orderId);
        return result == null ? null : ((Number) result).doubleValue();
    }

    /**
     * 根据套餐的id查询套餐金额
     */
    public static Double getPriceBySetmealId(Long setmealId) {
        Object result = queryOne("select price from setmeal where id = ?", "price", setmealId);
        return result == null ? null : ((Number) result).doubleValue();
    }

    /**
     * 清空指定用户的购物车
     * @param userId 用户ID
     */
    public static void clearCartByUserId(Long userId) {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, userId);
            int rows = stmt.executeUpdate();
            log.info("清空购物车成功, userId={}, 影响行数={}", userId, rows);
        } catch (SQLException e) {
            log.error("清空购物车失败, userId={}, SQL={}", userId, sql, e);
            throw new RuntimeException("清空购物车失败：" + e.getMessage(), e);
        }
    }
}