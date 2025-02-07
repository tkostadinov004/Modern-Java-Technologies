package bg.sofia.uni.fmi.mjt.splitwise.server.repository.converter;

import bg.sofia.uni.fmi.mjt.splitwise.server.data.CsvProcessor;
import bg.sofia.uni.fmi.mjt.splitwise.server.models.User;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.BeforeAll;

import java.io.StringReader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserConverterTest {
    private static CsvProcessor<User> csvProcessor = mock();

    @BeforeAll
    public static void setUp() {

    }
}
