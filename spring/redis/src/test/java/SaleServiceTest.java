import org.junit.jupiter.api.Test;
import org.reol.spring.redis.RedisApplication;
import org.reol.spring.redis.service.SaleService;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest(classes = RedisApplication.class)
public class SaleServiceTest {
    @Resource
    private SaleService saleService;

    @Test
    public void testStore(){
        saleService.setStore("101", 100);
    }

    @Test
    public void testSale() {
        for (int i = 0; i < 100; i++) {
            new Thread(() -> saleService.sale("101")).start();
        }
        while (true) {

        }


    }
}
