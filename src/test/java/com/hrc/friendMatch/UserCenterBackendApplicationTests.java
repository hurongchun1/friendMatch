package com.hrc.friendMatch;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import com.hrc.friendMatch.model.domain.User;
import com.hrc.friendMatch.service.UserService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

@SpringBootTest
class UserCenterBackendApplicationTests {
    @Resource
    UserService userService;
    @Resource
    RedissonClient redissonClient;

    //创建线程池
    private ExecutorService executorService = new ThreadPoolExecutor(60,10000,
            10000, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(10000));

    @Test
    void doConcurrentInsertUser() {
        final int INSERT_NUM = 100000;
        int batchSize = 10000;
        List<User> userList = new ArrayList<User>();
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        while(true){
            for (int i = 0; i < 10; i++) {
                //线程执行的任务，把10个线程需要执行的划分出来
                for (j = 0; j < INSERT_NUM; j++) {
                    User user = new User();
                    user.setUsername("假数据");
                    user.setUserAccount("shuju");
                    user.setAvatarUrl("aaa");
                    user.setGender(0);
                    user.setUserPassword("12345678");
                    user.setPhone("112344323");
                    user.setEmail("1233333");
                    user.setUserStatus(0);
                    user.setTags("['java','kafka']");
                    user.setUserRole(0);
                    user.setPlanetCode("111");
                    //原始的方法，一条一条插入，很慢很慢1000条数据也要 60s
                    //卡手点在于：
                    //1. 建立和释放数据库连接
                    //2. for循环是绝对线性的
//            userService.save(user);
                    userList.add(user);
                }
                if(j%batchSize == 0){
                    break;
                }
                //这里进行异步插入
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    System.out.println("threadName："+Thread.currentThread().getName());
                    userService.saveBatch(userList, batchSize);
                },executorService);
                futureList.add(future);
            }
            //一定要加这个join()，加上这个join()来阻塞主进程，不然的话就直接打印出来了
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).join();

            stopWatch.stop();
            System.out.println(stopWatch.getTotalTimeMillis());
        }
    }

    @Test
    public void insertUser(){
        final int INSERT_NUM = 100000;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<User> userList = new ArrayList<User>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假数据");
            user.setUserAccount("shuju");
            user.setAvatarUrl("aaa");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("112344323");
            user.setEmail("1233333");
            user.setUserStatus(0);
            user.setTags("['java','kafka']");
            user.setUserRole(0);
            user.setPlanetCode("111");
            //原始的方法，一条一条插入，很慢很慢1000条数据也要 60s
            //卡手点在于：
            //1. 建立和释放数据库连接
            //2. for循环是绝对线性的
//            userService.save(user);
            userList.add(user);
        }
        //batchSize：每次批量插入多少条数据，达到 1万数据插入量时 17s 10万数据量

        userService.saveBatch(userList,10000);
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }


    @Test
    public void redissonTest(){
        RList<Object> list = redissonClient.getList("test-redisson");
        list.add("nihao");
        System.out.println("list.get(0) = " + list.get(0));
    }
}
