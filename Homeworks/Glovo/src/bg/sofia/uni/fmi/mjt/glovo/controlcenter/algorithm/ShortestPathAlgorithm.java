package bg.sofia.uni.fmi.mjt.glovo.controlcenter.algorithm;

import bg.sofia.uni.fmi.mjt.glovo.controlcenter.map.Location;
import bg.sofia.uni.fmi.mjt.glovo.controlcenter.map.MapEntity;
import bg.sofia.uni.fmi.mjt.glovo.controlcenter.map.MapEntityType;

public class ShortestPathAlgorithm {
    private int minimumDistance = Integer.MAX_VALUE;
    private MapEntity[][] map;
    private boolean[][] visited;

    public ShortestPathAlgorithm(MapEntity[][] map) {
        this.map = map;
        this.visited = new boolean[map.length][map[0].length];
    }

    private boolean isValidPath(int x, int y) {
        return y >= 0 &&
                y < map.length &&
                x >= 0 &&
                x < map[y].length &&
                !visited[y][x] &&
                map[y][x].getType() != MapEntityType.WALL;
    }

    private int getClosestDistance(Location target, int x, int y, int currentDistance) {
        if (target.x() == x && target.y() == y) {
            minimumDistance = Math.min(minimumDistance, currentDistance);
            return minimumDistance;
        }
        visited[y][x] = true;
        if (isValidPath(x - 1, y)) {
            minimumDistance = getClosestDistance(target, x - 1, y, currentDistance + 1);
        }
        if (isValidPath(x, y + 1)) {
            minimumDistance = getClosestDistance(target, x, y + 1, currentDistance + 1);
        }
        if (isValidPath(x + 1, y)) {
            minimumDistance = getClosestDistance(target, x + 1, y, currentDistance + 1);
        }
        if (isValidPath(x, y - 1)) {
            minimumDistance = getClosestDistance(target, x, y - 1, currentDistance + 1);
        }
        visited[y][x] = false;
        return minimumDistance;
    }

    public int solve(Location target, Location source) {
        int result = getClosestDistance(target, source.x(), source.y(), 0);
        return result != Integer.MAX_VALUE ? result : -1;
    }
}
