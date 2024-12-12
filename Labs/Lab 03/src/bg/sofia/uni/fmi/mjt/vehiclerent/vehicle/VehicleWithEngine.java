package bg.sofia.uni.fmi.mjt.vehiclerent.vehicle;

import bg.sofia.uni.fmi.mjt.vehiclerent.exception.InvalidRentingPeriodException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public abstract sealed class VehicleWithEngine extends Vehicle permits Car, Caravan {
    private FuelType fuelType;
    private int numberOfSeats;
    private double pricePerWeek;
    protected VehicleWithEngine(String id, String model, FuelType fuelType, int numberOfSeats, double pricePerWeek, double pricePerDay, double pricePerHour) {
        super(id, model, pricePerDay, pricePerHour);
        setFuelType(fuelType);
        setNumberOfSeats(numberOfSeats);
        setPricePerWeek(pricePerWeek);
    }

    @Override
    public double calculateRentalPrice(LocalDateTime startOfRent, LocalDateTime endOfRent) throws InvalidRentingPeriodException {
        if(!canBeRented(startOfRent, endOfRent)) {
            throw new InvalidRentingPeriodException("This vehicle cannot be rented for this time period!");
        }

        long hoursRented = startOfRent.until(endOfRent, ChronoUnit.HOURS);
        double weeks = hoursRented /  (24 * 7);
        double days = (hoursRented % (24 * 7) / 24);
        double hours = (hoursRented - (weeks * 24 * 7) - (days * 24));
        return weeks * getPricePerWeek() + days * getPricePerDay() + hours * getPricePerHour() + (getFuelType().getPricePerDay() * days) + (getNumberOfSeats() * 5) + getDriver().group().getPrice();
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public double getPricePerWeek() {
        return pricePerWeek;
    }

    public void setPricePerWeek(double pricePerWeek) {
        this.pricePerWeek = pricePerWeek;
    }
}
