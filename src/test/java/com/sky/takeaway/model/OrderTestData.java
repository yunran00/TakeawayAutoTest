package com.sky.takeaway.model;

import lombok.Data;

@Data
public class OrderTestData {
    private Integer addressBookId;
    private Double amount;
    private Integer deliveryStatus;
    private String estimatedDeliveryTime;
    private Integer packAmount;
    private Integer payMethod;
    private String remark;
    private Integer tablewareNumber;
    private Integer tablewareStatus;
    private Integer expectedCode;
}
