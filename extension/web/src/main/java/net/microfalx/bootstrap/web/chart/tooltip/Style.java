package net.microfalx.bootstrap.web.chart.tooltip;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class Style {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fontSize;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fontFamily;


}
