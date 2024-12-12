package bg.sofia.uni.fmi.mjt.glovo.controlcenter;

import bg.sofia.uni.fmi.mjt.glovo.controlcenter.algorithm.ShortestPathAlgorithm;
import bg.sofia.uni.fmi.mjt.glovo.controlcenter.algorithm.UnreasonablePlacementAlgorithm;
import bg.sofia.uni.fmi.mjt.glovo.controlcenter.comparator.DeliveryComparator;
import bg.sofia.uni.fmi.mjt.glovo.controlcenter.map.DeliveryGuyEntity;
import bg.sofia.uni.fmi.mjt.glovo.controlcenter.map.Location;
import bg.sofia.uni.fmi.mjt.glovo.controlcenter.map.MapEntity;
import bg.sofia.uni.fmi.mjt.glovo.controlcenter.map.MapEntityType;
import bg.sofia.uni.fmi.mjt.glovo.delivery.DeliveryInfo;
import bg.sofia.uni.fmi.mjt.glovo.delivery.ShippingMethod;
import bg.sofia.uni.fmi.mjt.glovo.exception.IllegalMapLayoutException;
import bg.sofia.uni.fmi.mjt.glovo.exception.IrregularMapDimensionsException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ControlCenter implements ControlCenterApi {
    private MapEntity[][] map;
    private Set<Location> clients;
    private Map<Location, Set<DeliveryGuyEntity>> restaurants;
    private Map<Location, MapEntityType> deliveryGuys;

    private static MapEntityType mapEntityTypeFactory(char mapSymbol) {
        return switch (mapSymbol) {
            case '.' -> MapEntityType.ROAD;
            case '#' -> MapEntityType.WALL;
            case 'R' -> MapEntityType.RESTAURANT;
            case 'C' -> MapEntityType.CLIENT;
            case 'A' -> MapEntityType.DELIVERY_GUY_CAR;
            case 'B' -> MapEntityType.DELIVERY_GUY_BIKE;
            default -> throw new IllegalArgumentException("Invalid map symbol!");
        };
    }

    public ControlCenter(char[][] mapLayout) {
        map = new MapEntity[mapLayout.length][];
        clients = new LinkedHashSet<>();
        restaurants = new HashMap<>();
        deliveryGuys = new LinkedHashMap<>();

        int columns = -1;
        for (int i = 0; i < mapLayout.length; i++) {
            if (columns != -1 && mapLayout[i].length != columns) {
                throw new IrregularMapDimensionsException("Glovo map should be rectangular!");
            }
            columns = mapLayout[i].length;

            map[i] = new MapEntity[mapLayout[i].length];
            for (int j = 0; j < mapLayout[i].length; j++) {
                if (mapLayout[i][j] == 'C') {
                    clients.add(new Location(j, i));
                } else if (mapLayout[i][j] == 'R') {
                    restaurants.putIfAbsent(new Location(j, i), new LinkedHashSet<>());
                } else if (mapLayout[i][j] == 'A' || mapLayout[i][j] == 'B') {
                    deliveryGuys.putIfAbsent(new Location(j, i), mapEntityTypeFactory(mapLayout[i][j]));
                }
                map[i][j] = new MapEntity(new Location(j, i), mapEntityTypeFactory(mapLayout[i][j]));
            }
        }

        if (clients.isEmpty()) {
            throw new IllegalMapLayoutException("The map contains no clients!");
        }
        if (restaurants.isEmpty()) {
            throw new IllegalMapLayoutException("The map contains no restaurants!");
        }
        if (deliveryGuys.isEmpty()) {
            throw new IllegalMapLayoutException("The map contains no delivery guys!");
        }

        UnreasonablePlacementAlgorithm validation = new UnreasonablePlacementAlgorithm(map);
        if (validation.hasUnreasonablyPlacedEntities(clients)
                || validation.hasUnreasonablyPlacedEntities(restaurants.keySet())
                || validation.hasUnreasonablyPlacedEntities(deliveryGuys.keySet())) {
            throw new IllegalMapLayoutException("Some entities in the map are unreachable!");
        }
    }

    private boolean isSatisfactoryEntity(DeliveryGuyEntity entity, double maxPrice, int maxTime) {
        boolean isPriceSatisfied = maxPrice == -1 || entity.getPrice() <= maxPrice;
        boolean isTimeSatisfied = maxTime == -1 || entity.getTime() <= maxTime;

        return isPriceSatisfied && isTimeSatisfied;
    }

    private DeliveryGuyEntity getFirstValidDeliveryGuy(TreeSet<DeliveryGuyEntity> deliveryGuys,
                                                       double maxPrice,
                                                       int maxTime) {
        while (!deliveryGuys.isEmpty() && !isSatisfactoryEntity(deliveryGuys.first(), maxPrice, maxTime)) {
            deliveryGuys.removeFirst();
        }
        return deliveryGuys.pollFirst();
    }

    @Override
    public DeliveryInfo findOptimalDeliveryGuy(Location restaurantLocation,
                                               Location clientLocation,
                                               double maxPrice,
                                               int maxTime,
                                               ShippingMethod shippingMethod) {
        int closestDistanceFromRestaurantToCustomer =
                new ShortestPathAlgorithm(map).solve(clientLocation, restaurantLocation);
        if (closestDistanceFromRestaurantToCustomer == -1) {
            return null;
        }

        for (var deliveryGuyLocation : deliveryGuys.keySet()) {
            int closestDistanceToCurrentRestaurant =
                    new ShortestPathAlgorithm(map).solve(restaurantLocation, deliveryGuyLocation);
            if (closestDistanceToCurrentRestaurant == -1) {
                continue;
            }
            DeliveryGuyEntity entity = new DeliveryGuyEntity(
                    new MapEntity(deliveryGuyLocation, deliveryGuys.get(deliveryGuyLocation)),
                    closestDistanceToCurrentRestaurant,
                    closestDistanceFromRestaurantToCustomer);
            restaurants.get(restaurantLocation).add(entity);
        }

        TreeSet<DeliveryGuyEntity> optimalOrdering = new TreeSet<>(DeliveryComparator.of(shippingMethod));
        optimalOrdering.addAll(restaurants.get(restaurantLocation));

        DeliveryGuyEntity optimalDeliveryGuy = getFirstValidDeliveryGuy(optimalOrdering, maxPrice, maxTime);
        return optimalDeliveryGuy == null ? null :
                new DeliveryInfo(optimalDeliveryGuy.getLocation(), optimalDeliveryGuy.getPrice(),
                optimalDeliveryGuy.getTime(),
                optimalDeliveryGuy.getDeliveryType());
    }

    @Override
    public MapEntity[][] getLayout() {
        return map;
    }
}
