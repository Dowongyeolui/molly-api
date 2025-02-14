package org.example.mollyapi.cart.repository;

import org.example.mollyapi.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long>, CartCustomRepository{
    boolean existsByCartIdAndUserUserId(Long cartId, Long userId);
    Optional<Cart> findByProductItemIdAndUserUserId(Long itemId, Long userId);
    int countByUserUserId(Long userId);
    void deleteByCartIdAndUserUserId(Long cartId, Long userId);
    Optional<Cart> findByCartIdAndUserUserId(Long cartId, Long userId);
}
