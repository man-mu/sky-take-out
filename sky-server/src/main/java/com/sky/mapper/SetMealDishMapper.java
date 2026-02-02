package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetMealDishMapper {


    /**
     * 根据菜品id查询套餐id
     * @param id
     * @return
     */
    @Select("select setmeal_id from setmeal_dish where dish_id = #{id}")
    Long getSetMealsIdByDishId(Long id);
}
