package net.microfalx.bootstrap.web.chart;

import net.microfalx.bootstrap.web.chart.plot.Bar;
import net.microfalx.bootstrap.web.chart.plot.PlotOptions;
import net.microfalx.bootstrap.web.chart.series.Series;
import net.microfalx.bootstrap.web.chart.style.Stroke;
import net.microfalx.bootstrap.web.chart.tooltip.Tooltip;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ChartTest {

    @Test
    void createDefaultChart() {
        Chart chart = Chart.create(Type.BAR);
        assertNotNull(chart.getOptions());
        assertNotNull(chart.getId());
        assertEquals(Type.BAR, chart.getOptions().getType());

        chart = Chart.create(Options.create(Type.AREA));
        assertNotNull(chart.getOptions());

        assertEquals(Type.AREA, chart.getOptions().getType());
    }

    @Test
    void defaultChartToJson() {
        Chart chart = Chart.create(Type.BAR);
        assertThat(chart.toJson()).isEqualToNormalizingNewlines("""
                {
                  "chart" : {
                    "type" : "bar"
                  }
                }""");
    }

    @Test
    void pieSparkLine() {
        Options options = Options.create(Type.PIE).sparkline().setHeight("120px");
        Chart chart = Chart.create(options).setName("Records");
        chart.addSeries(net.microfalx.bootstrap.web.chart.series.Series.create(3, 10, 7));
        chart.setLabels("L7/OK", "L4/CON", "L4/TOUT");
        chart.setStroke(Stroke.width(1)).setTooltip(Tooltip.fixed(false));
        assertThat(chart.toJson()).isEqualToNormalizingNewlines("""
                {
                  "stroke" : {
                    "width" : 1.0
                  },
                  "series" : [ 3, 10, 7 ],
                  "labels" : [ "L7/OK", "L4/CON", "L4/TOUT" ],
                  "tooltip" : {
                    "fixed" : {
                      "enabled" : false
                    }
                  },
                  "chart" : {
                    "type" : "pie",
                    "height" : "120px",
                    "sparkline" : {
                      "enabled" : true
                    }
                  }
                }""");
    }

    @Test
    void barSparkLine() {
        Options options = Options.create(Type.BAR).sparkline().setHeight("120px");
        Chart chart = Chart.create(options).setName("Records");
        chart.setPlotOptions(PlotOptions.bar(new Bar().setColumnWidth("80%")))
                .addSeries(Series.create(25, 66, 41, 89, 63, 25, 44, 12, 36, 9, 54)).addDefaultLabels()
                .setTooltip(Tooltip.onlyValue());
        assertThat(chart.toJson()).isEqualToNormalizingNewlines("""
                {
                  "labels" : [ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11" ],
                  "plotOptions" : {
                    "bar" : {
                      "columnWidth" : "80%"
                    }
                  },
                  "tooltip" : {
                    "x" : {
                      "show" : false
                    },
                    "y" : {
                      "title" : {
                        "formatter" : function(seriesName) {
                          return ''
                        }
                      }
                    },
                    "fixed" : {
                      "enabled" : false
                    }
                  },
                  "series" : [ {
                    "data" : [ 25, 66, 41, 89, 63, 25, 44, 12, 36, 9, 54 ]
                  } ],
                  "chart" : {
                    "type" : "bar",
                    "height" : "120px",
                    "sparkline" : {
                      "enabled" : true
                    }
                  }
                }""");
    }

}