package net.microfalx.bootstrap.web.chart.xaxis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class DatetimeFormatter {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String year;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String month;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String day;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String hour;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String minute;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String second;

}
