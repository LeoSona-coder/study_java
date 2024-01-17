package org.reol.spring.redis.dal.mysql.product;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.reol.spring.redis.dal.dataobject.Product;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
