package net.microfalx.bootstrap.web.chart.toolbar;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Csv {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String filename;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String columnDelimiter;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String headerCategory;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String headerValue;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String dateFormatter;

}
