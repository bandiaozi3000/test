package com.hotel.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestApplicationTests {

    @Autowired
    AsyncDemo asyncDemo;

    @Autowired
    ExceptionDemo exceptionDemo;

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
        people.setPrice((double) 456);
        System.out.println(people);
    }

    @Test
    public void test6() {
        System.out.println(Integer.compare(1, 2));

    }

    @Test
    public void test7() {
        String result = AESUtil.decrypt("QvZ6RZcyDGBGvyaeRUY2du3dutHdgv3w+wCntRmQ40q7Q2bjDTkX7l3HqRCL2+VRZfPKA1+7fWhphN79vnDOuOmwLGgN1A50Htq0yR1ODq5Nn/z0UkWHMd+60AV+rtz3zp+cqWNp5nDUI2zxu/fXDyu7pqWyoRtz9uQNY/HSIkUoSwobtZ1byTt4eyB7j8EdzOwNhvbYLMx7fCBIaPknbw==",
                "C5i\\/6HEq3n+dwp4u\\/juLMA==", "tiL4uYcmeb7IJH73fdTE8g==", "UTF-8");
        System.out.println(result);
    }

    @Test
    public void test8() {
        System.out.println(UUID.randomUUID());
    }

    @Test
    public void test9() {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        Collections.reverse(list);
        System.out.println(list);
        Collections.shuffle(list);
        System.out.println(list);
        Collections.sort(list);
        System.out.println(list);
        Collections.reverse(list);
        System.out.println(list);
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return -Integer.compare(Integer.parseInt(o1), Integer.parseInt(o2));
            }
        });
        System.out.println(list);
        Collections.swap(list, 1, 2);
        System.out.println(list);
        Collections.rotate(list, 1);
        System.out.println(list);
    }

    @Test
    public void test10() {
        ObjectMapper objectMapper = new ObjectMapper();
        TestDemo test = new TestDemo();
        List<String> list = new ArrayList<>(1);
        list.add("da");
        list.add("da");
        list.add("da");
        list.add("da");
        System.out.println(list.size());
        String a[] = {"1"};
        String b[] = {"2"};
        String c[] = Arrays.copyOf(a, 8);
        System.out.println(Arrays.toString(c));
    }

    @Test
    public void test11() throws InterruptedException {
        TestDemo testDemo = new TestDemo();
        testDemo.test();
        testDemo.test();
    }

    @Test
    public void testAsync() {
        System.out.println(asyncDemo.hashCode());
        System.out.println(asyncDemo.hashCode());
        System.out.println(asyncDemo.hashCode());
        System.out.println(asyncDemo.hashCode());
        asyncDemo.equals(asyncDemo);
        String a = "1dadas";
        a.equals("dadasb");
    }

    @Test
    public void testException() throws Exception {
        System.out.println(exceptionDemo.caculate());

    }

    @Test
    public void testData(){
        DataDemo dataDemo = new DataDemo();
        dataDemo.setData("aa");
        System.out.println(dataDemo.getData());
    }

}
