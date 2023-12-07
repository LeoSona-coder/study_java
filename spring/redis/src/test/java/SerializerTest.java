import org.junit.jupiter.api.Test;
import org.reol.spring.redis.RedisApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@SpringBootTest(classes = RedisApplication.class)
public class SerializerTest {
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 测试redis的序列化，如果不配置序列化，则设置的key 和 value 都可读性很差
     */
    @Test
    public void test() {
        redisTemplate.opsForValue().set("name","张三");
    }
}
