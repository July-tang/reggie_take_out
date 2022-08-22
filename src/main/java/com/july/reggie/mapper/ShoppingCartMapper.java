package com.july.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.july.reggie.entity.ShoppingCart;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author july
 */
@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {

    @Insert("insert into shopping_cart " +
            "(id,name,user_id,dish_id,setmeal_id,dish_flavor,number,amount,image,create_time) " +
            "select #{id}, #{name}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{image}, #{createTime}" +
            "where not exists (select #{id} from shopping_cart where user_id = #{userId} and name = #{name})")
    void saveIfAbsent(ShoppingCart shoppingCart);
}
