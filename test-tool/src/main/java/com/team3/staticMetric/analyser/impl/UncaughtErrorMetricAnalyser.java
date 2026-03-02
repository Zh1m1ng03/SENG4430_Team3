package com.team3.staticMetric.analyser.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.TryStmt;
import com.team3.staticMetric.analyser.IMetricAnalyser;
import com.team3.staticMetric.analyser.Result;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UncaughtErrorMetricAnalyser implements IMetricAnalyser {

    @Override
    public String id() {
        return "UNCAUGHTERROR";
    }

    @Override
    public String description() {
        return "Average uncaught error-prone statements per method";
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

        int totalUncaughtStatements = 0;

        for (MethodDeclaration method : methods) {
            totalUncaughtStatements += countUncaughtErrors(method);
        }

        double avg = (double) totalUncaughtStatements / methods.size();

        return new Result(id(), "File", avg);
    }

    private int countUncaughtErrors(MethodDeclaration method) {
        if (method.getBody().isEmpty()) {
            return 0;
        }

        var body = method.getBody().get();

        // Count all method calls in this method
        List<MethodCallExpr> allMethodCalls = body.findAll(MethodCallExpr.class);

        // Only count top-level try blocks (exclude nested ones to avoid double counting)
        List<TryStmt> tryStatements = body.findAll(TryStmt.class);
        int callsInTry = 0;

        for (TryStmt tryStmt : tryStatements) {
            // 修复：使用 findAncestor 而不是 findAncestorOfType
            boolean isNested = tryStmt.findAncestor(TryStmt.class).isPresent();
            if (!isNested) {
                callsInTry += tryStmt.getTryBlock().findAll(MethodCallExpr.class).size();
            }
        }

        // Uncaught = total calls - calls protected by try-catch
        return Math.max(0, allMethodCalls.size() - callsInTry);
    }
}