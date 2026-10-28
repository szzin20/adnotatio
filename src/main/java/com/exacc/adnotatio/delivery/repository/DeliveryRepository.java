package com.exacc.adnotatio.delivery.repository;

import com.exacc.adnotatio.delivery.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, String> {
}
