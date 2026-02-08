package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealMapper;
import com.sky.mapper.ShopingCartMapper;
import com.sky.service.ShopingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ShopingCartServiceImpl implements ShopingCartService {

    @Autowired
    private ShopingCartMapper shopingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetMealMapper setMealMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Transactional
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //判断当前添加的菜品是否存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shopingCartMapper.list(shoppingCart);

        //存在数量加一
        if (list != null && !list.isEmpty()){
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shopingCartMapper.updateNumberById(cart);
        }else{
            //不存在,添加菜品
            //判断添加的是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            Long setmealId = shoppingCartDTO.getSetmealId();
            if (dishId != null){
                //添加的是菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());

            }else{
                //添加的是套餐
                Setmeal setmeal = setMealMapper.getById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shopingCartMapper.insert(shoppingCart);
        }

    }

    @Override
    public List<ShoppingCart> showShopingCarts() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> list = shopingCartMapper.list(shoppingCart);
        return list;
    }

    /**
     * 清空购物车
     */
    @Override
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        shopingCartMapper.deleteByUserId(userId);
    }

    /**
     * 删除购物车一个商品
     * @param shoppingCartDTO
     */
    @Transactional
    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        //获取用户当前购物车数据
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list = shopingCartMapper.list(shoppingCart);

        if (list != null && !list.isEmpty()){
            ShoppingCart cart = list.get(0);

            if (cart.getNumber() == 1){
                //如果数量为1,则删除该数据
                shopingCartMapper.deleteById(cart.getId());
            }else{
                //数量不为1,则数量减一
                cart.setNumber(cart.getNumber() - 1);
                shopingCartMapper.updateNumberById(cart);
            }
        }
    }

}
