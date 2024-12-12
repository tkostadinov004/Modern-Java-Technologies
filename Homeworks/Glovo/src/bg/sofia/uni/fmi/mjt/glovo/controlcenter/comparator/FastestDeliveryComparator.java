package bg.sofia.uni.fmi.mjt.glovo.controlcenter.comparator;

import bg.sofia.uni.fmi.mjt.glovo.controlcenter.map.DeliveryGuyEntity;

public class FastestDeliveryComparator implements DeliveryComparator {
    @Override
    public int compare(DeliveryGuyEntity o1, DeliveryGuyEntity o2) {
        return Integer.compare(o1.getTime(), o2.getTime());
    }
}
