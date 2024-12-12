package bg.sofia.uni.fmi.mjt.glovo.controlcenter.map;

public enum MapEntityType {
    ROAD('.'),
    WALL('#'),
    RESTAURANT('R'),
    CLIENT('C'),
    DELIVERY_GUY_CAR('A'),
    DELIVERY_GUY_BIKE('B');

    private char mapSymbol;
    private MapEntityType(char mapSymbol) {
        this.mapSymbol = mapSymbol;
    }

    public char getMapSymbol() {
        return mapSymbol;
    }
}