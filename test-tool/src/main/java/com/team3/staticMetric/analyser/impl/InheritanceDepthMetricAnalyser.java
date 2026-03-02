package com.team3.staticMetric.analyser.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.team3.staticMetric.analyser.IMetricAnalyser;
import com.team3.staticMetric.analyser.Result;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public final class InheritanceDepthMetricAnalyser implements IMetricAnalyser {

    @Override
    public String id() {
        return "DIT_AVG";
    }

    @Override
    public String description() {
        return "Average inheritance depth (DIT) per type";
    }

    @Override
    public Result run(CompilationUnit cu) {
        return analyze(cu);
    }

    private Result analyze(CompilationUnit cu) {
        List<ClassOrInterfaceDeclaration> allTypes = cu.findAll(ClassOrInterfaceDeclaration.class);

        if (allTypes.isEmpty()) {
            return new Result(id(), "File", 0.0);
        }

        int totalDepth = 0;

        for (ClassOrInterfaceDeclaration type : allTypes) {
            totalDepth += inheritanceDepth(type, allTypes, new HashSet<>());
        }

        double avg = (double) totalDepth / allTypes.size();

        return new Result(id(), "File", avg);
    }

    private int inheritanceDepth(ClassOrInterfaceDeclaration type,
                                 List<ClassOrInterfaceDeclaration> allTypes,
                                 Set<String> visiting) {
        String name = type.getNameAsString();

        if (!visiting.add(name)) {
            // Cycle detected; treat as leaf to avoid infinite recursion.
            return 1;
        }

        List<ClassOrInterfaceType> superTypes = new ArrayList<>();
        superTypes.addAll(type.getExtendedTypes());
        superTypes.addAll(type.getImplementedTypes());

        int maxParentDepth = 0;

        for (ClassOrInterfaceType superType : superTypes) {
            String parentName = superType.getNameAsString();

            Optional<ClassOrInterfaceDeclaration> parentDecl = allTypes.stream()
                    .filter(c -> c.getNameAsString().equals(parentName))
                    .findFirst();

            if (parentDecl.isPresent()) {
                int parentDepth = inheritanceDepth(parentDecl.get(), allTypes, visiting);
                if (parentDepth > maxParentDepth) {
                    maxParentDepth = parentDepth;
                }
            }
        }

        visiting.remove(name);

        return 1 + maxParentDepth;
    }
}
