package net.microfalx.bootstrap.web.chart.plot;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;
import net.microfalx.bootstrap.web.chart.plot.candlestick.Colors;
import net.microfalx.bootstrap.web.chart.plot.candlestick.Wick;

@Data
@ToString
public class Candlestick {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Colors colors;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Wick wick;


}
