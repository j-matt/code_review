package com.sii.repository;


import com.sii.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Inventory findByFlightNumberAndFlightDate(String flightNumber, String flightDate);

}
