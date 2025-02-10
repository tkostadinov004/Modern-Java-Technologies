package bg.sofia.uni.fmi.mjt.glovo.controlcenter.comparator;

import bg.sofia.uni.fmi.mjt.glovo.controlcenter.map.DeliveryGuyEntity;
import bg.sofia.uni.fmi.mjt.glovo.delivery.ShippingMethod;

import java.util.Comparator;

public interface DeliveryComparator extends Comparator<DeliveryGuyEntity> {
    static DeliveryComparator of(ShippingMethod shippingMethod) {
        return switch(shippingMethod) {
            case FASTEST -> new FastestDeliveryComparator();
            case CHEAPEST -> new CheapestDeliveryComparator();
        };
    }
}
