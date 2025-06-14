package com.hrc.friendMatch.job;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrc.friendMatch.model.domain.User;
import com.hrc.friendMatch.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @FileName: PreCacheJob
 * @Description:
 * @Author: hrc
 * @CreateTime: 2025/5/10 21:46
 * @Version: 1.0.0
 */
@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserService userService;
    @Resource
    private RedissonClient redissonClient;

    private List<Integer> userIds = Arrays.asList(5);

    @Scheduled(cron = "0 0 0 1 * ? ")
    public void doCacheRecommendUser() {
        ValueOperations<String, String> valueOperations = stringRedisTemplate.opsForValue();
        String lockKey = "doCache:Recommend:Lock";
        RLock lock = redissonClient.getLock(lockKey);
        boolean tryLock = false;
        try {
            tryLock = lock.tryLock(0, 30000L, TimeUnit.MILLISECONDS);
            if (tryLock) {
                for (Integer userId : userIds) {
                    //判断缓存中是否存在数据
                    String key = StrUtil.format("yupao:user:recommend:{}", userId);
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userIPage = new Page<>(1, 20);
                    Page<User> userPage = userService.page(userIPage, queryWrapper);
                    //还需要将查询到的数据，添加到redis中
                    try {
                        //因为缓存及时添加失败了，也不应该导致程序报错，走统一异常处理
                        valueOperations.set(key, JSONUtil.toJsonStr(userPage), 30, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        log.error("缓存添加失败！！", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }finally {
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }
}

