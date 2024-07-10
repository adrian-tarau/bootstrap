package net.microfalx.bootstrap.web.chart.animation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Animations {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean enabled;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Easing easing;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double speed;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private AnimateGradually animateGradually;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private DynamicAnimation dynamicAnimation;

    public static Animations disable() {
        return new Animations().setEnabled(false);
    }

}
