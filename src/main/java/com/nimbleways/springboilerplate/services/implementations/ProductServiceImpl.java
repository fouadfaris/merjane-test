package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.services.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.repositories.ProductRepository;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Override
    public void handleSeasonalProduct(Product product) {

        LocalDate now = LocalDate.now();
        boolean isInSeason = now.isAfter(product.getSeasonStartDate()) && now.isBefore(product.getSeasonEndDate());

        if(isInSeason) {
            if(product.getAvailable() > 0) {
                int available = product.getAvailable() - 1;
                product.setAvailable(available);
                productRepository.save(product);
            }else if (now.plusDays(product.getLeadTime()).isAfter(product.getSeasonEndDate())) {
                notificationService.sendOutOfStockNotification(product.getName());
                product.setAvailable(0);
                productRepository.save(product);
            } else {
                notifyDelay(product.getLeadTime(), product);
            }
        }else {
            notificationService.sendOutOfStockNotification(product.getName());
        }
    }

    @Override
    public void handleExpiredProduct(Product product) {

        LocalDate now = LocalDate.now();

        if (now.isBefore(product.getExpiryDate())) {
            if (product.getAvailable() > 0) {
                int available = product.getAvailable() - 1;
                product.setAvailable(available);
                productRepository.save(product);
            } else {
                notificationService.sendOutOfStockNotification(product.getName());
            }
        }else {
            notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
            product.setAvailable(0);
            productRepository.save(product);
        }
    }

    @Override
    public void handleNormalProduct(Product product) {
        if(product.getAvailable() > 0) {
            int available = product.getAvailable() - 1;
            product.setAvailable(available);
            productRepository.save(product);
        }else {
            int leadTime = product.getLeadTime();
            if(leadTime > 0) {
                notifyDelay(leadTime, product);
            }
        }
    }

    public void notifyDelay(int leadTime, Product product) {
        product.setLeadTime(leadTime);
        productRepository.save(product);
        notificationService.sendDelayNotification(leadTime, product.getName());
    }
}