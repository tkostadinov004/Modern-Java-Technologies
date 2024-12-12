package bg.sofia.uni.fmi.mjt.vehiclerent.vehicle;

import bg.sofia.uni.fmi.mjt.vehiclerent.exception.InvalidRentingPeriodException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class Caravan extends VehicleWithEngine{
    private int numberOfBeds;
    public Caravan(String id, String model, FuelType fuelType, int numberOfSeats, int numberOfBeds, double pricePerWeek, double pricePerDay, double pricePerHour) {
        super(id, model, fuelType, numberOfSeats, pricePerWeek, pricePerDay, pricePerHour);
        setNumberOfBeds(numberOfBeds);
    }
    @Override
    public double calculateRentalPrice(LocalDateTime startOfRent, LocalDateTime endOfRent) throws InvalidRentingPeriodException {
        if(!canBeRented(startOfRent, endOfRent)) {
            throw new InvalidRentingPeriodException("This vehicle cannot be rented for this time period!");
        }
        return super.calculateRentalPrice(startOfRent, endOfRent) + (numberOfBeds * 10);
    }

    @Override
    public boolean canBeRented(LocalDateTime startOfRent, LocalDateTime endOfRent) {
        return startOfRent.compareTo(endOfRent) < 0 && startOfRent.until(endOfRent, ChronoUnit.DAYS) >= 1;
    }

    public int getNumberOfBeds() {
        return numberOfBeds;
    }

    public void setNumberOfBeds(int numberOfBeds) {
        this.numberOfBeds = numberOfBeds;
    }
}
