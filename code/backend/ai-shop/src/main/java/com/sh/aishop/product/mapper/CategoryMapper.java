package com.sh.aishop.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sh.aishop.common.entity.Category;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT COUNT(*) > 0 FROM category WHERE shop_id = #{shopId} AND name = #{name} AND deleted = 0")
    boolean existsByShopIdAndName(@Param("shopId") Long shopId, @Param("name") String name);

    @Select("SELECT COUNT(*) > 0 FROM category WHERE shop_id = #{shopId} AND name = #{name} AND id != #{excludeId} AND deleted = 0")
    boolean existsByShopIdAndNameExcluding(@Param("shopId") Long shopId, @Param("name") String name, @Param("excludeId") Long excludeId);
}