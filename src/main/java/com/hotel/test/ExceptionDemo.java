package com.hotel.test;

import org.springframework.stereotype.Component;

@Component
public class ExceptionDemo {

    public String caculate() throws Exception {
//        try{
//            result = "a";
            int a=10/0;
//            return result;
//        }finally {
//            result = "b";
//        }
        return "ffafa";
    }
}
