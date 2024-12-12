package bg.sofia.uni.fmi.mjt.vehiclerent.driver;

public enum AgeGroup {
    JUNIOR(10),
    EXPERIENCED(0),
    SENIOR(15);

    int price;
    AgeGroup(int price) {
        this.price = price;
    }
    public int getPrice() {
        return price;
    }
}
