package com.sky.takeaway.testcase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.takeaway.model.OrderTestData;
import org.junit.jupiter.params.provider.Arguments;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

public class OrderTestDataProvider {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Stream<Arguments> positiveDataProvider() throws Exception {
        return filterDataByExpectedCode(1);
    }

    public static Stream<Arguments> negativeDataProvider() throws Exception {
        return filterDataByExpectedCode(0);
    }

    private static Stream<Arguments> filterDataByExpectedCode(int expectedCode) throws Exception {
        File jsonFile = new File("src/test/resources/data/order_test_data.json");
        OrderTestData[] allData = objectMapper.readValue(jsonFile, OrderTestData[].class);

        return Arrays.stream(allData)
                .filter(data -> data.getExpectedCode() == expectedCode)
                .map(data -> Arguments.of(data));
    }
}