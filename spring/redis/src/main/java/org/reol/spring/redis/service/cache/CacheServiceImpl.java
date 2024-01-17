package org.reol.spring.redis.service.cache;

import org.reol.spring.redis.config.RedisConstant;
import org.reol.spring.redis.dal.dataobject.Product;
import org.reol.spring.redis.dal.mysql.product.ProductMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class CacheServiceImpl implements CacheService {
    @Resource
    private ProductMapper productMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 简单的添加缓存的办法
     *
     * @param id
     * @return
     */
    @Override
    public Product getProductById(String id) {
        return getProductByAddCache(id);
    }


    @Override
    public Product updateProduct(Product product) {
        String productId = product.getProductId();
        if (productId == null) {
            return null;
        }
        // 先更新 再删除缓存
        productMapper.updateById(product);

        redisTemplate.delete(RedisConstant.CACHE_PRODUCT_KEY + productId);
        return product;
    }

    /**
     * 解决缓存穿透问题
     * @param id
     * @return
     */
    public Product getProductByPassThrough(String id) {
        String key = RedisConstant.CACHE_PRODUCT_KEY + id;
        // 1 从redis中查询商铺缓存
        Product product = (Product)redisTemplate.opsForValue().get(key);
//        2 判断缓存是否存在
        if (product != null && !product.equals(Product.EMPTY_PRODUCT)) {
            // 存在直接返回
            return product;
        }
        // 判断命中的是否是空值
        if (product != null) {
            return null;
        }
        // 查询数据库
        product = productMapper.selectById(id);
        // 如果不存在，则设置空对象并且返回null
        if (product == null) {
            redisTemplate.opsForValue().set(key, Product.EMPTY_PRODUCT, RedisConstant.CACHE_SHOP_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        // 如果存在，就构建缓存
        redisTemplate.opsForValue().set(key, product, RedisConstant.CACHE_SHOP_NULL_TTL, TimeUnit.MINUTES);
        return product;
    }


    /**
     * 查询产品 同时添加缓存
     *
     * @param id
     * @return
     */
    private Product getProductByAddCache(String id) {
        // 1 先从redis中查询是否有缓存
        Product product = (Product) redisTemplate.opsForValue().get(RedisConstant.CACHE_PRODUCT_KEY + id);
        // 2 判断是否存在
        if (product != null) {
//             3 存在则直接返回
            return product;
        }
        // 4 没有缓存就从数据库中查询
        product = productMapper.selectById(id);
        if (product == null) {
            return null;
        }
        // 5 如果数据库中存在 就重新构建缓存
        redisTemplate.opsForValue().set(RedisConstant.CACHE_PRODUCT_KEY + id, product);
        return product;
    }

}
