package bg.sofia.uni.fmi.mjt.glovo.controlcenter.algorithm;

import bg.sofia.uni.fmi.mjt.glovo.controlcenter.map.Location;
import bg.sofia.uni.fmi.mjt.glovo.controlcenter.map.MapEntity;
import bg.sofia.uni.fmi.mjt.glovo.controlcenter.map.MapEntityType;

import java.util.EnumSet;
import java.util.Set;

public class UnreasonablePlacementAlgorithm {
    private MapEntity[][] map;
    private boolean[][] visited;

    public UnreasonablePlacementAlgorithm(MapEntity[][] map) {
        this.map = map;
        this.visited = new boolean[map.length][map[0].length];
    }

    private boolean isLocationOnTheMap(int x, int y) {
        return y >= 0 &&
                y < map.length &&
                x >= 0 &&
                x < map[y].length;
    }

    private boolean isActiveEntity(int x, int y) {
        return map[y][x].getType() != MapEntityType.ROAD && map[y][x].getType() != MapEntityType.WALL;
    }

    private void visitSurroundings(Set<MapEntityType> visitedEntities, Location entityLocation, int x, int y) {
        if (!isLocationOnTheMap(x, y) || visited[y][x] || map[y][x].getType() == MapEntityType.WALL) {
            return;
        }
        if ((entityLocation.y() != y || entityLocation.x() != x) && map[y][x].getType() != MapEntityType.ROAD) {
            visitedEntities.add(map[y][x].getType());
        }

        visited[y][x] = true;

        visitSurroundings(visitedEntities, entityLocation, x - 1, y);
        visitSurroundings(visitedEntities, entityLocation, x, y + 1);
        visitSurroundings(visitedEntities, entityLocation, x + 1, y);
        visitSurroundings(visitedEntities, entityLocation, x, y - 1);
    }

    private boolean isUnreasonablyPlaced( Set<MapEntityType> visitedEntities) {
        Set<MapEntityType> allEntities = EnumSet.copyOf(visitedEntities);
        allEntities.remove(MapEntityType.WALL);
        return !(allEntities.contains(MapEntityType.RESTAURANT) &&
                    (allEntities.contains(MapEntityType.DELIVERY_GUY_CAR) ||
                    allEntities.contains(MapEntityType.DELIVERY_GUY_BIKE)));
    }

    public boolean hasUnreasonablyPlacedEntities(Set<Location> entityLocations) {
        for (Location location : entityLocations) {
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[i].length; j++) {
                    visited[i][j] = false;
                }
            }

            Set<MapEntityType> visitedEntities = EnumSet.of(map[location.y()][location.x()].getType());
            visitSurroundings(visitedEntities, location, location.x(), location.y());
            if (isUnreasonablyPlaced(visitedEntities)) {
                return true;
            }
        }
        return false;
    }
}
