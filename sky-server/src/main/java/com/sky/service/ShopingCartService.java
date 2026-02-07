package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShopingCartService {

    void add(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> showShopingCarts();
}
