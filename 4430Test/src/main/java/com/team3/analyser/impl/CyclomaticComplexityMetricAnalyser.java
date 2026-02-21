package com.team3.analyser.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;
import com.team3.analyser.IMetricAnalyser;
import com.team3.analyser.Result;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class CyclomaticComplexityMetricAnalyser implements IMetricAnalyser {

    @Override
    public String id() {
        return "CC_AVG";
    }

    @Override
    public String description() {
        return "Average cyclomatic complexity per method";
    }

    @Override
    public Result run(CompilationUnit cu) {
        return analyze(cu);
    }

    private Result analyze(CompilationUnit cu) {
        List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);

        if (methods.isEmpty()) {
            return new Result(id(), "File", 0.0);
        }

        int total = 0;

        for (MethodDeclaration m : methods) {
            total += cyclomaticComplexity(m);
        }

        double avg = (double) total / methods.size();

        return new Result(id(), "File", avg);
    }

    private int cyclomaticComplexity(MethodDeclaration method) {

        int cc = 1;

        if (method.getBody().isEmpty()) {
            return cc;
        }

        var body = method.getBody().get();

        cc += body.findAll(IfStmt.class).size();
        cc += body.findAll(ForStmt.class).size();
        cc += body.findAll(ForEachStmt.class).size();
        cc += body.findAll(WhileStmt.class).size();
        cc += body.findAll(DoStmt.class).size();
        cc += body.findAll(CatchClause.class).size();

        return cc;
    }
}