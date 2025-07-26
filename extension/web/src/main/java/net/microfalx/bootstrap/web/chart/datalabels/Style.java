package net.microfalx.bootstrap.web.chart.datalabels;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Style {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fontSize;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fontFamily;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> colors;

}
