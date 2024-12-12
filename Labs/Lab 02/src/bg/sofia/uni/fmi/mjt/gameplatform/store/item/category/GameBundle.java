package bg.sofia.uni.fmi.mjt.gameplatform.store.item.category;

import bg.sofia.uni.fmi.mjt.gameplatform.store.item.StoreItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

public class GameBundle extends BaseCategory implements StoreItem {
    private Game[] games;

    public GameBundle(String title, BigDecimal price, LocalDateTime releaseDate, Game[] games) {
        super(title, price, releaseDate);
        setGames(games);
    }
    public Game[] getGames() {
        return Arrays.copyOf(games, games.length);
    }
    public void setGames(Game[] games) {
        this.games = new Game[games.length];
        for (int i = 0; i < games.length; i++) {
            this.games[i] = games[i];
        }
    }
    @Override
    public double getRating() {
        if(games.length == 0) {
            return 0;
        }

        double avg = 0.0;
        for(Game game : games) {
            avg += game.getRating();
        }
        return avg / games.length;
    }
    @Override
    public void rate(double rating) {
        for (int i = 0; i < games.length; i++) {
            this.games[i].rate(rating);
        }
    }
}