package com.seng4430;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.seng4430.metrics.IMetric;
import com.seng4430.metrics.MetricFactory;
import com.seng4430.result.MetricResult;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        String code = """
                class Test {
                    void methodA(int x) {
                        if (x > 0) {
                            System.out.println("Positive");
                        }

                        for (int i = 0; i < 10; i++) {
                            System.out.println(i);
                        }
                    }

                    void methodB() {
                        while (true) {
                            break;
                        }
                    }
                }
                """;

        CompilationUnit cu = StaticJavaParser.parse(code);

        // Option 1: create one metric by name
        IMetric metric = MetricFactory.create("CC_AVG");

        MetricResult result = metric.analyze(cu);

        System.out.println(metric.description() + ": " + result.value());

    }
}