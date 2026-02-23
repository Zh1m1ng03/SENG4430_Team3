package com.team3.io.impl;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.team3.io.JavaSourceReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class JavaParserSourceReader implements JavaSourceReader {

    @Override
    public CompilationUnit read(Path path) throws IOException {
        if (path == null || !Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Not a file or not found: " + path);
        }
        return StaticJavaParser.parse(path);
    }
}
