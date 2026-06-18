package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.entities.Product;

public interface ProductService {
    void handleSeasonalProduct(Product product);

    void handleExpiredProduct(Product product);

    void handleNormalProduct(Product product);
}
