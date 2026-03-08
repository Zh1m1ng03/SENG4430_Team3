package com.seng4430.qulitytesttool;

import com.seng4430.qulitytesttool.shared.cli.MenuController;
import com.seng4430.qulitytesttool.shared.config.AnalysisConfig;
import com.seng4430.qulitytesttool.staticanalysis.engine.StaticAnalysisEngine;
import com.seng4430.qulitytesttool.staticanalysis.metric.MetricRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AnalysisConfig.class)
public class QulityTestToolApplication implements CommandLineRunner {

    @Autowired
    private AnalysisConfig config;

    public static void main(String[] args) {
        SpringApplication.run(QulityTestToolApplication.class, args);
    }

    @Override
    public void run(String... args) {
        StaticAnalysisEngine engine = new StaticAnalysisEngine(MetricRegistry.getAll(), config);
        MenuController menu = new MenuController(engine, MetricRegistry.getAll());
        menu.start();
    }
}
