package com.inv1ct0.testCase_1;

public interface SuperEncoder {
    byte[] serialize(Object anyBeans);
    Object deserialize(byte[] data);
}