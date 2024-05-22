package net.microfalx.bootstrap.web.controller;

import net.microfalx.bootstrap.web.chart.Chart;
import net.microfalx.bootstrap.web.chart.ChartNotFoundException;
import net.microfalx.bootstrap.web.chart.ChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/chart")
@Controller
public class ChartController {

    @Autowired
    private ChartService chartService;

    @GetMapping("/render/{id}")
    public ResponseEntity<Object> get(Model model, @PathVariable("id") String id) {
        try {
            Chart chart = chartService.get(id);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(chart.toJson());
        } catch (ChartNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
