package bg.sofia.uni.fmi.mjt.gameplatform.store;

import bg.sofia.uni.fmi.mjt.gameplatform.store.item.StoreItem;
import bg.sofia.uni.fmi.mjt.gameplatform.store.item.category.Game;
import bg.sofia.uni.fmi.mjt.gameplatform.store.item.filter.ItemFilter;

import java.math.BigDecimal;
import java.util.Arrays;

public class GameStore implements StoreAPI{
    private StoreItem[] availableItems;
    private boolean is100YOapplied;
    private boolean isVAN40applied;

    public GameStore(StoreItem[] availableItems) {
        setAvailableItems(availableItems);
    }
    private boolean applyFilters(StoreItem item, ItemFilter[] itemFilters) {
        boolean result = true;
        for(ItemFilter filter : itemFilters) {
            result &= filter.matches(item);
        }
        return result;
    }
    @Override
    public StoreItem[] findItemByFilters(ItemFilter[] itemFilters) {
        StoreItem[] result = new StoreItem[availableItems.length];
        int index = 0;
        for(StoreItem item : availableItems) {
            if(applyFilters(item, itemFilters)) {
                result[index++] = item;
            }
        }
        return Arrays.copyOf(result, index);
    }

    private void applyDiscount(int percentage) {
        for (int i = 0; i < availableItems.length; i++) {
            availableItems[i].setPrice(availableItems[i].getPrice().multiply(BigDecimal.valueOf(1).subtract(BigDecimal.valueOf(percentage).divide(BigDecimal.valueOf(100)))));
        }
    }
    @Override
    public void applyDiscount(String promoCode) {
        if(promoCode.equals("VAN40") && !isVAN40applied) {
           applyDiscount(40);
           isVAN40applied = true;
        }
        else if(promoCode.equals("100YO") && !is100YOapplied) {
            applyDiscount(100);
            is100YOapplied = true;
        }
    }

    @Override
    public boolean rateItem(StoreItem item, int rating) {
        if (rating < 1 || rating > 5) {
            return false;
        }
        item.rate(rating);
        return true;
    }

    public StoreItem[] getAvailableItems() {
        return Arrays.copyOf(availableItems, availableItems.length);
    }

    public void setAvailableItems(StoreItem[] availableItems) {
        this.availableItems = new StoreItem[availableItems.length];
        for (int i = 0; i < availableItems.length; i++) {
            this.availableItems[i] = availableItems[i];
        }
    }
}