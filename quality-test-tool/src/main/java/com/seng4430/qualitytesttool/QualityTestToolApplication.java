package com.seng4430.qualitytesttool;

import com.seng4430.qualitytesttool.shared.cli.MenuController;
import com.seng4430.qualitytesttool.shared.config.AnalysisConfig;
import com.seng4430.qualitytesttool.staticanalysis.engine.StaticAnalysisEngine;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricAnalyser;
import com.seng4430.qualitytesttool.staticanalysis.metric.MetricRegistry;
import com.seng4430.qualitytesttool.dynamicanalysis.service.RunTimeTestService;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AnalysisConfig.class)
public class QualityTestToolApplication implements CommandLineRunner {

    @Autowired
    private AnalysisConfig config;

    @Autowired
    private RunTimeTestService runTimeTestService;

    @Value("${app.cli.enabled:true}")
    private boolean cliEnabled;

    public static void main(String[] args) {
        SpringApplication.run(QualityTestToolApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (!cliEnabled) {
            return;
        }
        List<MetricAnalyser> metrics = MetricRegistry.getAll(config);
        StaticAnalysisEngine engine = new StaticAnalysisEngine(config);
        MenuController menu = new MenuController(engine, metrics, runTimeTestService);
        menu.start();
    }
}
