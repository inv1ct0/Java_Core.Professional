package com.inv1ct0.testCase_1;

public class CycleLinkException extends Throwable {
     public String getMessage() {
            return "Serialization is impossible! There are Cycle links!";
        }
    }