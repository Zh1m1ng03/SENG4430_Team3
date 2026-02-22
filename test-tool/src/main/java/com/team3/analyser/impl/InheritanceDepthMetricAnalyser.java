package com.team3.analyser.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.team3.analyser.IMetricAnalyser;
import com.team3.analyser.Result;
import org.springframework.stereotype.Service;

@Service
public final class InheritanceDepthMetricAnalyser implements IMetricAnalyser {

    @Override
    public String id() {
        return "INHERIT_DEPTH";
    }

    @Override
    public String description() {
        return "Depth of inheritance tree within a file";
    }

    @Override
    public Result run(CompilationUnit cu) {
        return new Result(id(), "File", 0.0);
    }
}
