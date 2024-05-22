package net.microfalx.bootstrap.web.chart.annotations;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public class Annotations {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String position;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<YAxisAnnotations> yaxis;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<XAxisAnnotations> xaxis;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PointAnnotations> points;

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public List<YAxisAnnotations> getYaxis() {
        return yaxis;
    }

    public void setYaxis(List<YAxisAnnotations> yaxis) {
        this.yaxis = yaxis;
    }

    public List<XAxisAnnotations> getXaxis() {
        return xaxis;
    }

    public void setXaxis(List<XAxisAnnotations> xaxis) {
        this.xaxis = xaxis;
    }

    public List<PointAnnotations> getPoints() {
        return points;
    }

    public void setPoints(List<PointAnnotations> points) {
        this.points = points;
    }
}
