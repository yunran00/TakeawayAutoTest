package com.sky.takeaway.config;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;

import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class TestConfig {

    protected static Properties props = new Properties();

    @BeforeAll
    public static void setup() {
        try {
            // 1. 加载配置文件
            InputStream input = TestConfig.class.getClassLoader()
                    .getResourceAsStream("config/application.properties");
            if (input == null) {
                throw new RuntimeException("配置文件 config/application.properties 未找到！");
            }
            props.load(input);
            input.close();

            // 2. 设置全局 BaseURI（换环境只改这一个地方）
            String baseUrl = props.getProperty("base.url");
            if (baseUrl != null && !baseUrl.isEmpty()) {
                RestAssured.baseURI = baseUrl;
                log.info("已加载配置：base.url = {}", baseUrl);
            }

            log.info("TestConfig 初始化完成");

        } catch (Exception e) {
            throw new RuntimeException("TestConfig 加载失败，请检查配置文件路径", e);
        }
    }

    protected static String getProperty(String key, String defaultValue) {
        String value = props.getProperty(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
}