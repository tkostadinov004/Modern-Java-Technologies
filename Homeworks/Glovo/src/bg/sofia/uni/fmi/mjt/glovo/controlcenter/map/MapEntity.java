package bg.sofia.uni.fmi.mjt.glovo.controlcenter.map;

import java.util.Objects;

public class MapEntity {
    private Location location;
    private MapEntityType type;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapEntity mapEntity = (MapEntity) o;
        return Objects.equals(location, mapEntity.location) && type == mapEntity.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, type);
    }

    public MapEntity(Location location, MapEntityType type) {
        setLocation(location);
        setType(type);
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public MapEntityType getType() {
        return type;
    }

    public void setType(MapEntityType type) {
        this.type = type;
    }
}
