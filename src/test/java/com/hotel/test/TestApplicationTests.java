package com.hotel.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Test
    public void test() {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        list = list.subList(0, 3);
        System.out.println(list);


    }

    @Test
    public void test2() throws InterruptedException {
        Date date = new Date();
        Thread.sleep(10000);
        Date date1 = new Date();
        System.out.println(date.before(date1));
    }

    @Test
    public void test3() throws JsonProcessingException {
        List<People> list = new ArrayList<>();
        People people = new People();
        for (int i = 0; i < 5; i++) {
            people.setId(i);
            list.add(people);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(list));
    }

    @Test
    public void test4() throws JsonProcessingException {
        int a = 1;
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            a = i;
            list.add(a);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(list));
    }

    @Test
    public void Test5() throws JsonProcessingException {
        People people = new People();
        people.setPrice((double)456);
        System.out.println(people);
    }

    @Test
    public void test6(){
        System.out.println(Integer.compare(1,2));

    }

    @Test
    public void test7(){
        String result = AESUtil.decrypt("QvZ6RZcyDGBGvyaeRUY2du3dutHdgv3w+wCntRmQ40q7Q2bjDTkX7l3HqRCL2+VRZfPKA1+7fWhphN79vnDOuOmwLGgN1A50Htq0yR1ODq5Nn/z0UkWHMd+60AV+rtz3zp+cqWNp5nDUI2zxu/fXDyu7pqWyoRtz9uQNY/HSIkUoSwobtZ1byTt4eyB7j8EdzOwNhvbYLMx7fCBIaPknbw==",
                "C5i\\/6HEq3n+dwp4u\\/juLMA==","tiL4uYcmeb7IJH73fdTE8g==","UTF-8");
        System.out.println(result);
    }

}
