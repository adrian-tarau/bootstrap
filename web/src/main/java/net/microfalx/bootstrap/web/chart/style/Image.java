package net.microfalx.bootstrap.web.chart.style;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Image {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> src;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double width;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double height;

}
