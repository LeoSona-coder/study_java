package org.reol.spring.redis.controller;

import org.reol.spring.redis.dal.dataobject.Product;
import org.reol.spring.redis.service.cache.CacheService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/cache")
public class CacheController {
    @Resource
    private CacheService cacheService;
    @GetMapping("/get")
    public Product get(@RequestParam("id") String id) {
        return cacheService.getProductById(id);
    }
    @PutMapping("/update")
    public Product update(@RequestBody Product product) {
        return cacheService.updateProduct(product);
    }
}
