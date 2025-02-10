package bg.sofia.uni.fmi.mjt.glovo;

import bg.sofia.uni.fmi.mjt.glovo.controlcenter.ControlCenter;
import bg.sofia.uni.fmi.mjt.glovo.controlcenter.ControlCenterApi;
import bg.sofia.uni.fmi.mjt.glovo.controlcenter.map.Location;
import bg.sofia.uni.fmi.mjt.glovo.controlcenter.map.MapEntity;
import bg.sofia.uni.fmi.mjt.glovo.delivery.Delivery;
import bg.sofia.uni.fmi.mjt.glovo.delivery.DeliveryInfo;
import bg.sofia.uni.fmi.mjt.glovo.delivery.ShippingMethod;
import bg.sofia.uni.fmi.mjt.glovo.exception.InvalidOrderException;
import bg.sofia.uni.fmi.mjt.glovo.exception.NoAvailableDeliveryGuyException;

public class Glovo implements GlovoApi {
    ControlCenterApi controlCenterApi;

    public Glovo(char[][] mapLayout) {
        controlCenterApi = new ControlCenter(mapLayout);
    }

    private boolean isLocationOnTheMap(Location location, MapEntity[][] map) {
        return location.y() >= 0 &&
                location.y() < map.length &&
                location.x() >= 0 &&
                location.x() < map[location.y()].length;
    }

    private boolean isMapEntityValid(MapEntity entity) {
        MapEntity[][] map = controlCenterApi.getLayout();
        Location entityLocation = entity.getLocation();
        return map[entityLocation.y()][entityLocation.x()].getType() == entity.getType();
    }

    private static void validateArguments(MapEntity client,
                                          MapEntity restaurant,
                                          String foodItem,
                                          ShippingMethod shippingMethod) {
        if (client == null) {
            throw new InvalidOrderException("Client cannot be null!");
        }
        if (restaurant == null) {
            throw new InvalidOrderException("Restaurant cannot be null!");
        }
        if (shippingMethod == null) {
            throw new InvalidOrderException("Shipping method cannot be null!");
        }
        if (foodItem == null || foodItem.isBlank() || foodItem.isEmpty()) {
            throw new InvalidOrderException("Food item name cannot be null, blank or empty!");
        }
    }

    private Delivery handleDelivery(MapEntity client,
                                    MapEntity restaurant,
                                    String foodItem,
                                    double maxPrice,
                                    int maxTime,
                                    ShippingMethod shippingMethod)
            throws NoAvailableDeliveryGuyException {
        validateArguments(client, restaurant, foodItem, shippingMethod);
        if (!isLocationOnTheMap(client.getLocation(), controlCenterApi.getLayout())) {
            throw new InvalidOrderException("Client location is outside the map boundaries");
        }
        if (!isLocationOnTheMap(restaurant.getLocation(), controlCenterApi.getLayout())) {
            throw new InvalidOrderException("Restaurant location is outside the map boundaries");
        }
        if (!isMapEntityValid(client)) {
            throw new InvalidOrderException("The map doesn't contain a client at the provided coordinates!");
        }
        if (!isMapEntityValid(restaurant)) {
            throw new InvalidOrderException("The map doesn't contain a restaurant at the provided coordinates!");
        }

        DeliveryInfo info = controlCenterApi.findOptimalDeliveryGuy(restaurant.getLocation(),
                client.getLocation(),
                maxPrice,
                maxTime,
                shippingMethod);
        if (info == null) {
            throw new NoAvailableDeliveryGuyException("No delivery person is available for fulfilling this order!");
        }
        return new Delivery(client.getLocation(),
                restaurant.getLocation(),
                info.deliveryGuyLocation(),
                foodItem,
                info.price(),
                info.estimatedTime());
    }

    @Override
    public Delivery getCheapestDelivery(MapEntity client,
                                        MapEntity restaurant,
                                        String foodItem)
            throws NoAvailableDeliveryGuyException {
        return handleDelivery(client, restaurant, foodItem, -1, -1, ShippingMethod.CHEAPEST);
    }

    @Override
    public Delivery getFastestDelivery(MapEntity client,
                                       MapEntity restaurant,
                                       String foodItem)
            throws NoAvailableDeliveryGuyException {
        return handleDelivery(client, restaurant, foodItem, -1, -1, ShippingMethod.FASTEST);
    }

    @Override
    public Delivery getFastestDeliveryUnderPrice(MapEntity client,
                                                 MapEntity restaurant,
                                                 String foodItem,
                                                 double maxPrice)
            throws NoAvailableDeliveryGuyException {
        return handleDelivery(client, restaurant, foodItem, maxPrice, -1, ShippingMethod.FASTEST);
    }

    @Override
    public Delivery getCheapestDeliveryWithinTimeLimit(MapEntity client,
                                                       MapEntity restaurant,
                                                       String foodItem,
                                                       int maxTime)
            throws NoAvailableDeliveryGuyException {
        return handleDelivery(client, restaurant, foodItem, -1, maxTime, ShippingMethod.CHEAPEST);
    }
}