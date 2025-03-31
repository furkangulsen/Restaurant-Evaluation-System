package gui_testleri;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public LocalDateTime unmarshal(String value) {
        return value != null ? LocalDateTime.parse(value, formatter) : null;
    }

    @Override
    public String marshal(LocalDateTime value) {
        return value != null ? value.format(formatter) : null;
    }
} 