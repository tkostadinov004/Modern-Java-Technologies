package bg.sofia.uni.fmi.mjt.splitwise.server.data;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.implementations.ExpensesCsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.Expense;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class CsvProcessor<T> {
    protected static final DateTimeFormatter DATETIME_PARSE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CSVReader reader;
    private final String filePath;
    private Set<T> data;

    public CsvProcessor(CSVReader reader, String filePath) {
        this.reader = reader;
        this.filePath = filePath;
    }

    protected synchronized Set<T> readAll(Function<String[], T> mappingFunction) {
        if (data != null) {
            return data;
        }

        Set<T> result;
        try (reader) {
            result = reader
                    .readAll()
                    .stream()
                    .map(mappingFunction)
                    .filter(obj -> obj != null)
                    .collect(Collectors.toSet());
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public abstract Set<T> readAll();

    protected synchronized void writeToFile(T obj, Function<T, String> mappingFunction) {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(filePath), StandardOpenOption.APPEND)) {
            String content = mappingFunction.apply(obj);
            writer.write(content);
            writer.write(System.lineSeparator());
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void writeToFile(T obj);

    public synchronized void remove(Predicate<T> predicate) {
        if (data == null) {
            data = readAll();
        }

        List<T> validObjects = data
                .stream()
                .filter(obj -> !predicate.test(obj))
                .toList();

        try {
            Files.deleteIfExists(Path.of(filePath));
        } catch (IOException e) {
            //throw new RuntimeException(e);
        }

        validObjects.forEach(this::writeToFile);
    }

    public synchronized void modify(Predicate<T> predicate, T newValue) {
        if (data == null) {
            data = readAll();
        }
        data = data
                .stream()
                .map(obj -> predicate.test(obj) ? newValue : obj)
                .collect(Collectors.toSet());

        try {
            Files.deleteIfExists(Path.of(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        data.forEach(this::writeToFile);
    }
}
