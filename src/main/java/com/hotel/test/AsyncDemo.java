package com.hotel.test;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
public class AsyncDemo {

    @Async
    public void asyncPrint() throws InterruptedException {
        Thread.sleep(5000);
        System.out.println("异步测试中...............");
    }

    public String  print() throws InterruptedException {
        asyncPrint();
        return "结束";

    }
}
