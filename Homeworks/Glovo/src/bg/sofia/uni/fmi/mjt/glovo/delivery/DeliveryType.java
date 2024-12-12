package bg.sofia.uni.fmi.mjt.glovo.delivery;

public enum DeliveryType {
    CAR(5, 3),
    BIKE(3, 5);

    private int pricePerKilometer;
    private int timePerKilometer;
    DeliveryType(int pricePerKilometer, int timePerKilometer) {
        this.pricePerKilometer = pricePerKilometer;
        this.timePerKilometer = timePerKilometer;
    }

    public int getPricePerKilometer() {
        return pricePerKilometer;
    }

    public int getTimePerKilometer() {
        return timePerKilometer;
    }
}
