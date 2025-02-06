package bg.sofia.uni.fmi.mjt.splitwise.server.repository.implementations.converter;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;

import java.util.Objects;
import java.util.stream.Collector;

public abstract class DataConverter<Output, StoredType, DTO> {
    private final CsvProcessor<DTO> csvProcessor;

    public DataConverter(CsvProcessor<DTO> csvProcessor) {
        this.csvProcessor = csvProcessor;
    }

    public Output populate(Collector<StoredType, ?, Output> collector) {
        return csvProcessor
                .readAll()
                .stream()
                .map(this::createFromDTO)
                .filter(Objects::nonNull)
                .collect(collector);
    }

    public abstract StoredType createFromDTO(DTO dto);
}
