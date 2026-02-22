package com.team3.analyser.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.team3.analyser.IMetricAnalyser;
import com.team3.analyser.Result;
import org.springframework.stereotype.Service;

@Service
public final class DuplicateCodeRatioMetricAnalyser implements IMetricAnalyser {

    @Override
    public String id() {
        return "DUP_RATIO";
    }

    @Override
    public String description() {
        return "Duplicate code ratio within a file";
    }

    @Override
    public Result run(CompilationUnit cu) {
        return new Result(id(), "File", 0.0);
    }
}