import org.junit.jupiter.api.Test;
import org.reol.spring.redis.RedisApplication;
import org.reol.spring.redis.dal.dataobject.Product;
import org.reol.spring.redis.service.cache.CacheService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

@SpringBootTest(classes = RedisApplication.class)
public class CacheTest {
    @Resource
    private CacheService cacheService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Test
    public void testStore(){
        Product product = cacheService.getProductByPassThrough("20231207004");
        System.out.println(product);
    }
}
