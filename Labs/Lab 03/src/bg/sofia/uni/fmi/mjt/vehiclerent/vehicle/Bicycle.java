package bg.sofia.uni.fmi.mjt.vehiclerent.vehicle;

import bg.sofia.uni.fmi.mjt.vehiclerent.exception.InvalidRentingPeriodException;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;

public final class Bicycle extends Vehicle{
    public Bicycle(String id, String model, double pricePerDay, double pricePerHour){
        super(id, model, pricePerDay, pricePerHour);
    }
    @Override
    public double calculateRentalPrice(LocalDateTime startOfRent, LocalDateTime endOfRent) throws InvalidRentingPeriodException {
        if(!canBeRented(startOfRent, endOfRent)) {
            throw new InvalidRentingPeriodException("This vehicle cannot be rented for this time period!");
        }

        long hoursRented = startOfRent.until(endOfRent, ChronoUnit.HOURS);
        if(hoursRented < 1) {
            return getPricePerHour();
        }
        return hoursRented / 24 * getPricePerDay() + hoursRented % 24 * getPricePerHour();
    }

    @Override
    public boolean canBeRented(LocalDateTime startOfRent, LocalDateTime endOfRent) {
        return startOfRent.compareTo(endOfRent) < 0 && startOfRent.until(endOfRent, ChronoUnit.DAYS) < 7;
    }
}
