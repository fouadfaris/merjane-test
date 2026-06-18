package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductTypeEnum;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@UnitTest
public class ProductTests {

    @Mock
    private NotificationService notificationService;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    public void normal_inStock_decrementsAvailable() {
        Product product = new Product(null, 15, 5, ProductTypeEnum.NORMAL, "USB Cable", null, null, null);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        productService.handleNormalProduct(product);

        assertEquals(4, product.getAvailable());
        Mockito.verify(productRepository).save(product);
        Mockito.verifyNoInteractions(notificationService);
    }

    @Test
    public void normal_outOfStock_notifiesDelay() {
        Product product = new Product(null, 15, 0, ProductTypeEnum.NORMAL, "USB Cable", null, null, null);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        productService.handleNormalProduct(product);

        Mockito.verify(notificationService).sendDelayNotification(15, "USB Cable");
        Mockito.verify(productRepository).save(product);
    }

    @Test
    public void normal_outOfStockWithZeroLeadTime_doesNothing() {
        Product product = new Product(null, 0, 0, ProductTypeEnum.NORMAL, "USB Cable", null, null, null);

        productService.handleNormalProduct(product);

        Mockito.verifyNoInteractions(notificationService);
        Mockito.verifyNoInteractions(productRepository);
    }

    @Test
    public void seasonal_inSeasonAndInStock_decrementsAvailable() {
        Product product = new Product(null, 15, 5, ProductTypeEnum.SEASONAL, "Watermelon",
                null, LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        Mockito.when(productRepository.save(product)).thenReturn(product);

        productService.handleSeasonalProduct(product);

        assertEquals(4, product.getAvailable());
        Mockito.verify(productRepository).save(product);
        Mockito.verifyNoInteractions(notificationService);
    }

    @Test
    public void seasonal_inSeasonOutOfStock_leadTimeWithinSeason_notifiesDelay() {
        Product product = new Product(null, 10, 0, ProductTypeEnum.SEASONAL, "Watermelon",
                null, LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        Mockito.when(productRepository.save(product)).thenReturn(product);

        productService.handleSeasonalProduct(product);

        Mockito.verify(notificationService).sendDelayNotification(10, "Watermelon");
        Mockito.verify(productRepository).save(product);
    }

    @Test
    public void seasonal_inSeasonOutOfStock_leadTimeExceedsSeason_notifiesOutOfStock() {
        Product product = new Product(null, 50, 0, ProductTypeEnum.SEASONAL, "Watermelon",
                null, LocalDate.now().minusDays(1), LocalDate.now().plusDays(10));
        Mockito.when(productRepository.save(product)).thenReturn(product);

        productService.handleSeasonalProduct(product);

        assertEquals(0, product.getAvailable());
        Mockito.verify(notificationService).sendOutOfStockNotification("Watermelon");
        Mockito.verify(productRepository).save(product);
    }

    @Test
    public void seasonal_outOfSeason_notifiesOutOfStock() {
        Product product = new Product(null, 15, 5, ProductTypeEnum.SEASONAL, "Grapes",
                null, LocalDate.now().plusDays(180), LocalDate.now().plusDays(240));

        productService.handleSeasonalProduct(product);

        Mockito.verify(notificationService).sendOutOfStockNotification("Grapes");
        Mockito.verifyNoMoreInteractions(productRepository);
    }

    // ─── EXPIRABLE ────────────────────────────────────────────────────────────

    @Test
    public void expirable_notExpiredAndInStock_decrementsAvailable() {
        Product product = new Product(null, 15, 5, ProductTypeEnum.EXPIRABLE, "Butter",
                LocalDate.now().plusDays(10), null, null);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        productService.handleExpiredProduct(product);

        assertEquals(4, product.getAvailable());
        Mockito.verify(productRepository).save(product);
        Mockito.verifyNoInteractions(notificationService);
    }

    @Test
    public void expirable_expired_notifiesExpiration() {
        LocalDate expiryDate = LocalDate.now().minusDays(2);
        Product product = new Product(null, 90, 6, ProductTypeEnum.EXPIRABLE, "Milk",
                expiryDate, null, null);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        productService.handleExpiredProduct(product);

        assertEquals(0, product.getAvailable());
        Mockito.verify(notificationService).sendExpirationNotification("Milk", expiryDate);
        Mockito.verify(productRepository).save(product);
    }
}