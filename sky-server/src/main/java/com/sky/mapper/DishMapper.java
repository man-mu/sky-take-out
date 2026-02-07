package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 插入菜品数据
     * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);


    /**
     * 根据id删除菜品数据
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 修改菜品数据
     * @param dish
     */
    @AutoFill(value = OperationType.UPDATE)
    void updateDish(Dish dish);

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    List<Dish> getByCategoryId(String categoryId);

    /**
     * 动态条件查询菜品
     * @param dish
     * @return
     */
    List<Dish> list(Dish dish);

    /**
     * 根据id查询菜品和口味
     * @param id
     * @return
     */
    @Update("update dish set status = #{status} where id = #{id}")
    void startOrStop(Integer status, Long id);
}
