package net.microfalx.bootstrap.web.chart.series;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.lang.CollectionUtils;
import net.microfalx.lang.Nameable;

import java.util.Arrays;
import java.util.List;

import static net.microfalx.lang.ArgumentUtils.requireNonNull;

@Data
@ToString
public class Series<T> implements Nameable {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SeriesType type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<T> data;

    @SafeVarargs
    public static <T> Series<T> create(String name, T... data) {
        Series<T> series = create(data);
        return series.setName(name);
    }

    @SafeVarargs
    public static <T> Series<T> create(T... data) {
        requireNonNull(data);
        Series<T> series = new Series<>();
        series.setData(Arrays.asList(data));
        return series;
    }

    public static <T> Series<T> create(String name, Iterable<T> data) {
        Series<T> series = create(data);
        return series.setName(name);
    }

    public static <T> Series<T> create(Iterable<T> data) {
        requireNonNull(data);
        Series<T> series = new Series<>();
        series.setData(CollectionUtils.toList(data));
        return series;
    }

}
