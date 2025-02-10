package bg.sofia.uni.fmi.mjt.glovo.controlcenter.comparator;

import bg.sofia.uni.fmi.mjt.glovo.controlcenter.map.DeliveryGuyEntity;

public class CheapestDeliveryComparator implements DeliveryComparator {
    @Override
    public int compare(DeliveryGuyEntity o1, DeliveryGuyEntity o2) {
        return Double.compare(o1.getPrice(), o2.getPrice());
    }
}