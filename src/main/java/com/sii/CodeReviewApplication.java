package com.sii;

import com.sii.dto.BookingException;
import com.sii.dto.BookingStatus;
import com.sii.dto.Fare;
import com.sii.entity.BookingRecord;
import com.sii.entity.Flight;
import com.sii.entity.Inventory;
import com.sii.entity.Passenger;
import com.sii.repository.BookingRepository;
import com.sii.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;

import java.util.*;

@SpringBootApplication
@Slf4j
public class CodeReviewApplication implements CommandLineRunner {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    InventoryRepository inventoryRepository;

    public static void main(String[] args) {
        SpringApplication.run(CodeReviewApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {

        Inventory[] invs = {
                new Inventory("BF100", "22-JAN-22", 100),
                new Inventory("BF101", "22-JAN-22", 100),
                new Inventory("BF102", "22-JAN-22", 100),
                new Inventory("BF103", "22-JAN-22", 100),
                new Inventory("BF104", "22-JAN-22", 100),
                new Inventory("BF105", "22-JAN-22", 100),
                new Inventory("BF106", "22-JAN-22", 100)};
        Arrays.asList(invs).forEach(inventory -> inventoryRepository.save(inventory));



        BookingRecord booking = new BookingRecord("BF101", "NYC","SFO","22-JAN-22",new Date(),"101");
        List<Passenger> passengers = new ArrayList<>();
        passengers.add(new Passenger("Gean","Franc","Male", booking));
        //	passengers.add(new Passenger("Redi","Ivan","Female",booking));
        booking.setPassengers(passengers);
        long record  = book(booking);

        prepareFlights();
    }

    private List<Flight> prepareFlights(){
        return List.of(new Flight("NYC","SFO"),
                new Flight("NYC","SFO"),
                new Flight("NYC","SFO"),
                new Flight("NYC","SFO"),
                new Flight("NYC","SFO"),
                new Flight("NYC","SFO"));
    }

    private long book(BookingRecord record){
        //check fare
        log.info("calling inventory to get inventory");
        //check inventory
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
}
