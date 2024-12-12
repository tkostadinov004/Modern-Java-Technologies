package bg.sofia.uni.fmi.mjt.vehiclerent;

import bg.sofia.uni.fmi.mjt.vehiclerent.driver.AgeGroup;
import bg.sofia.uni.fmi.mjt.vehiclerent.driver.Driver;
import bg.sofia.uni.fmi.mjt.vehiclerent.exception.InvalidRentingPeriodException;
import bg.sofia.uni.fmi.mjt.vehiclerent.exception.VehicleNotRentedException;
import bg.sofia.uni.fmi.mjt.vehiclerent.exception.VehicleAlreadyRentedException;
import bg.sofia.uni.fmi.mjt.vehiclerent.vehicle.Bicycle;
import bg.sofia.uni.fmi.mjt.vehiclerent.vehicle.Car;
import bg.sofia.uni.fmi.mjt.vehiclerent.vehicle.FuelType;
import bg.sofia.uni.fmi.mjt.vehiclerent.vehicle.Vehicle;

import java.time.LocalDateTime;

public class RentalService {

    /**
     * Simulates renting of the vehicle. Makes all required validations and then the provided driver "rents" the provided
     * vehicle with start time @startOfRent
     * @throws IllegalArgumentException if any of the passed arguments are null
     * @throws VehicleAlreadyRentedException in case the provided vehicle is already rented
     * @param driver the designated driver of the vehicle
     * @param vehicle the chosen vehicle to be rented
     * @param startOfRent the start time of the rental
     */
    public void rentVehicle(Driver driver, Vehicle vehicle, LocalDateTime startOfRent) {
        if(driver == null) {
            throw new IllegalArgumentException("Driver cannot be null!");
        }
        if(vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null!");
        }
        if(startOfRent == null) {
            throw new IllegalArgumentException("Start of rent cannot be null!");
        }
        vehicle.rent(driver, startOfRent);
    }

    /**
     * This method simulates rental return - it includes validation of the arguments that throw the listed exceptions
     * in case of errors. The method returns the expected total price for the rental - price for the vehicle plus
     * additional tax for the driver, if it is applicable
     * @param vehicle the rented vehicle
     * @param endOfRent the end time of the rental
     * @return price for the rental
     * @throws IllegalArgumentException in case @endOfRent or @vehicle is null
     * @throws VehicleNotRentedException in case the vehicle is not rented at all
     * @throws InvalidRentingPeriodException in case the endOfRent is before the start of rental, or the vehicle
     * does not allow the passed period for rental, e.g. Caravans must be rented for at least a day.
     */
    public double returnVehicle(Vehicle vehicle, LocalDateTime endOfRent) throws InvalidRentingPeriodException {
       if(vehicle == null) {
           throw new IllegalArgumentException("Vehicle cannot be null!");
       }
       if(endOfRent == null) {
            throw new IllegalArgumentException("End of rent cannot be null!");
       }
       vehicle.returnBack(endOfRent);
       return vehicle.calculateRentalPrice(vehicle.getRentalStart(), endOfRent);
    }

    public static void main(String[] args) {
        RentalService rentalService = new RentalService();
        LocalDateTime rentStart = LocalDateTime.of(2024, 10, 10, 0, 0, 0);
        Driver experiencedDriver = new Driver(AgeGroup.EXPERIENCED);

       Vehicle bike = new Bicycle("id", "Drag4i", 100, 10);
       bike.rent(experiencedDriver, rentStart);
       try {
           rentalService.returnVehicle(bike, LocalDateTime.of(2024, 10, 17, 0, 0, 0));
       }
       catch(InvalidRentingPeriodException e) {
           System.out.println(e.getMessage());
       }
    }
}