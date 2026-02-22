package com.team3.io;

import com.github.javaparser.ast.CompilationUnit;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Reads a Java source file and returns a parsed AST.
 * Inject this wherever CompilationUnit from a file path is needed.
 */
public interface JavaSourceReader {

    /**
     * Parse a Java file into a CompilationUnit.
     *
     * @param path path to the .java file
     * @return parsed compilation unit
     * @throws IllegalArgumentException if path is not a readable file or parse fails
     */
    CompilationUnit read(Path path) throws IOException;
}
