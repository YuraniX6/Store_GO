package com.storego.cartService.repository;

import org.springframework.stereotype.Repository;
import com.storego.cartService.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

@Repository
public interface CartRepository
        extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUserId(Long userId);
}