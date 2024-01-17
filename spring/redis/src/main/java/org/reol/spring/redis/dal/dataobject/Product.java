package org.reol.spring.redis.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.reol.spring.redis.dal.base.BaseDO;
@TableName(value = "tb_product", autoResultMap = true)
// 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@EqualsAndHashCode(callSuper = true)
@Data
public class Product extends BaseDO {

    public static final Product EMPTY_PRODUCT = new Product();
    @TableId
    //产品id
    private String productId;
    //产品名称
    private String productName;

}
