package com.tfr.gmall.user;

import com.tfr.gmall.util.RedisUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
@RunWith(SpringRunner.class)
@SpringBootTest
class GmallUserServiceApplicationTests {

    @Autowired
    RedisUtil redisUtil;
    @Test
    void contextLoads() {
        Jedis jedis=redisUtil.getJedis();
        System.out.println(jedis);
    }

}
