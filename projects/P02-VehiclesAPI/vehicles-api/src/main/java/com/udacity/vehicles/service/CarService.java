package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.Address;
import com.udacity.vehicles.client.prices.Price;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService {

    private final CarRepository repository;
    private final WebClient mapsWebClient;
    private final WebClient pricingWebClient;
    private static final Logger log = LoggerFactory.getLogger(CarService.class);

    @Autowired
    public CarService(CarRepository repository, @Qualifier("maps") WebClient mapsWebClient, @Qualifier("pricing") WebClient pricingWebClient) {
        this.repository = repository;
        this.mapsWebClient = mapsWebClient;
        this.pricingWebClient = pricingWebClient;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    public List<Car> list() {
        return repository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    public Car findById(Long id) {
        Optional<Car> carToFind = repository.findById(id);
        if (carToFind.isEmpty()) {
            throw new CarNotFoundException("Car with id "+id+" does not exist");
        }
        Car car = carToFind.get();

        Price price = getPriceForCar(car.getId());
        if (price != null) {
            car.setPrice(price.getPrice().toPlainString());
        }

        Location location = car.getLocation();
        Address address = getAddressForCar(location);
        location.setAddress(address.getAddress());
        car.setLocation(location);


        return car;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    public Car save(Car car) {
        if (car.getId() != null) {
            return repository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        return repository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        car.setCreatedAt(LocalDateTime.now());
        car.setModifiedAt(LocalDateTime.now());
        return repository.save(car);
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    public void delete(Long id) {
        Optional<Car> carToDelete = repository.findById(id);
        if (carToDelete.isEmpty()){
            throw new CarNotFoundException("Car with id "+id+" does not exist");
        }
        repository.delete(carToDelete.get());
    }

    private Price getPriceForCar(Long carId) {
        try {
            return pricingWebClient.get().uri(uriBuilder -> uriBuilder.path("/services/price").queryParam("vehicleId", carId).build()).retrieve().bodyToMono(Price.class).block();
        } catch (Exception ex) {
            log.error("Something went wrong -> "+ex.getMessage());
            return null;
        }
    }

    private Address getAddressForCar(Location location) {
        return mapsWebClient.get().uri(uriBuilder -> uriBuilder
                .path("/maps")
                .queryParam("lat", location.getLat())
                .queryParam("lon", location.getLon())
                .build())
                .retrieve().bodyToMono(Address.class).single().block();
    }
}
