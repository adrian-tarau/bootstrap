package net.microfalx.bootstrap.web.chart.nodata;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Style {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String color;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fontSize;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fontFamily;

}
