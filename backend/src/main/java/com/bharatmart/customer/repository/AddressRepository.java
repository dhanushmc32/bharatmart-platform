package com.bharatmart.customer.repository;

import com.bharatmart.customer.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AddressRepository extends JpaRepository<AddressEntity, Long> {
    List<AddressEntity> findByCustomerId(Long customerId);
}
