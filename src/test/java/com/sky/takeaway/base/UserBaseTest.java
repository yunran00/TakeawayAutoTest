package com.sky.takeaway.base;

import com.sky.takeaway.config.TestConfig;
import com.sky.takeaway.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;

@Slf4j
public class UserBaseTest extends TestConfig {

    protected String token;
    protected Long userId = 4L;

    @BeforeEach
    public void setUp() {
        token = TokenUtils.getUserToken();
        log.info("获取到用户 token: {}", token);
        //TODO 后续可在此扩展：从 Token 解析 userId
        log.info("当前测试用户 ID: {}", userId);
    }
}