package bg.sofia.uni.fmi.mjt.glovo.controlcenter.map;

import bg.sofia.uni.fmi.mjt.glovo.delivery.DeliveryType;

import java.util.Objects;

public class DeliveryGuyEntity {
    private Location location;
    private DeliveryType deliveryType;
    private int time;
    private int price;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryGuyEntity that = (DeliveryGuyEntity) o;
        return time == that.time &&
                price == that.price &&
                Objects.equals(location, that.location) &&
                deliveryType == that.deliveryType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, deliveryType, time, price);
    }

    public DeliveryGuyEntity(MapEntity entity, int kilometersToRestaurant, int kilometersToCustomer) {
        location = entity.getLocation();
        deliveryType = switch (entity.getType()) {
            case DELIVERY_GUY_CAR -> DeliveryType.CAR;
            case DELIVERY_GUY_BIKE -> DeliveryType.BIKE;
            default -> throw new IllegalArgumentException("Invalid entity type!");
        };

        int totalDistance = kilometersToRestaurant + kilometersToCustomer;
        time = totalDistance * deliveryType.getTimePerKilometer();
        price = totalDistance * deliveryType.getPricePerKilometer();
    }

    public int getTime() {
        return time;
    }

    public int getPrice() {
        return price;
    }

    public Location getLocation() {
        return location;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }
}
