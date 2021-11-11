package com.sii.repository;

import com.sii.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchRepository extends JpaRepository<Flight, Long> {
    @Query("SELECT f FROM Flight f WHERE f.origin = :origin and f.destination = :destination")
    List<Flight> findByOriginAndDestination(@Param("origin") String origin,
                        @Param("destination") String destination);

}
