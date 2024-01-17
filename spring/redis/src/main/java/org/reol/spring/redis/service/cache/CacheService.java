package org.reol.spring.redis.service.cache;

import org.reol.spring.redis.dal.dataobject.Product;

public interface CacheService {
    Product getProductById(String id);

    Product updateProduct(Product product);
    Product getProductByPassThrough(String id);
}
