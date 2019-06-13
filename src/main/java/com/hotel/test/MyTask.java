package com.hotel.test;

import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by pangPython on 2017/9/21.
 */
public class MyTask {
    //设置超时时间
    private int timeout;

    public MyTask(int timeout) {
        this.timeout = timeout;
    }

    //处理我的任务
    public String handler(){
        String result = "-1";

        ExecutorService executorService = Executors.newCachedThreadPool();

        FutureTask<String> futureTask = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                /**
                 * 这里边可以做一些耗时的操作或者发送http之类的，有可能造成超时的任务
                 *
                 */
                Random random = new Random();
                int random_num = Math.abs(random.nextInt())%4000;
                System.out.println("random_num: "+random_num);
                Thread.sleep(random_num);
                return Integer.toString(random_num);
            }
        });
        //执行
        executorService.execute(futureTask);

        try {
            //获取结果
            result = futureTask.get(timeout,TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            //如果被中断
            System.out.println("InterruptedException");
            futureTask.cancel(true);
        } catch (ExecutionException e) {
            // 如果执行异常
            System.out.println("ExecutionException");
            futureTask.cancel(true);
        } catch (TimeoutException t){
            //如果超时
            System.out.println("time out!");
            result = "0";
            futureTask.cancel(true);
        } finally {
            //关闭
            executorService.shutdown();
        }

        return result;
    }
    public static void main(String[] args){
        //声明并实例化一个自己的任务，设置超时时间为3秒
        MyTask myTask = new MyTask(3000);
        //循环5次，测试，每次使用随机数在4000范围内，如果休眠时间大于3000，则会进入超时处理逻辑
        String result;
        for(int i = 0;i<5;i++){
            System.out.println("=======第"+i+"次========");
            result = myTask.handler();
            System.out.println(result);
        }

    }
}
