package com.sii.controller;

import com.sii.dto.BookingException;
import com.sii.dto.BookingStatus;
import com.sii.entity.BookingRecord;
import com.sii.entity.Flight;
import com.sii.entity.Inventory;
import com.sii.entity.Passenger;
import com.sii.repository.BookingRepository;
import com.sii.repository.InventoryRepository;
import com.sii.repository.SearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/booking")
@Slf4j
public class BookingController {

    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    InventoryRepository inventoryRepository;
    @Autowired
    SearchRepository searchRepository;

    @RequestMapping(value="/create" , method = RequestMethod.POST)
    long book(@RequestBody BookingRecord record){
        Inventory inventory = inventoryRepository.findByFlightNumberAndFlightDate(record.getFlightNumber(),record.getFlightDate());
        if(!inventory.isAvailable(record.getPassengers().size())){
            throw new BookingException("No more seats avaialble");
        }
        log.info("successfully checked inventory" + inventory);
        log.info("calling inventory to update inventory");
        //update inventory
        inventory.setAvailable(inventory.getAvailable() - record.getPassengers().size());
        inventoryRepository.saveAndFlush(inventory);
        log.info("sucessfully updated inventory");
        //save booking
        record.setStatus(BookingStatus.BOOKING_CONFIRMED);
        List<Passenger> passengers = record.getPassengers();
        passengers.forEach(passenger -> passenger.setBookingRecord(record));
        record.setBookingDate(new Date());
        long id=  bookingRepository.save(record).getId();
        log.info("Successfully saved booking");
        //send a message to search to update inventory
        log.info("sending a booking event");
        Map<String, Object> bookingDetails = new HashMap<String, Object>();
        bookingDetails.put("FLIGHT_NUMBER", record.getFlightNumber());
        bookingDetails.put("FLIGHT_DATE", record.getFlightDate());
        bookingDetails.put("NEW_INVENTORY", inventory.getBookableInventory());
        log.info("booking event successfully delivered "+ bookingDetails);
        return id;
    }

    @RequestMapping("/get/{id}")
    BookingRecord getBooking(@PathVariable long id){
        return bookingRepository.findById(id).get();
    }

    @RequestMapping("/search/{origin}/{destination}")
    public ResponseEntity<List<Flight>> searchFlights(@PathVariable String origin, @PathVariable String destination){
        return ResponseEntity.ok(searchRepository.findByOriginAndDestination(origin, destination));
    }


}
