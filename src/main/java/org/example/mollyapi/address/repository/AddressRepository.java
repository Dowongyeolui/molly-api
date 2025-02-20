package org.example.mollyapi.address.repository;

import org.example.mollyapi.address.entity.Address;
import org.example.mollyapi.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserOrderByDefaultAddrDesc(User user);
    Optional<Address> findByUserAndDefaultAddr(User user, boolean defaultAddr);
}