package com.team3.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Resolves a path (single file or project directory) to a list of Java source files
 * to analyse (e.g. under controller, service, dao). Inject this where file discovery is needed.
 */
public interface JavaFileFinder {

    /**
     * Returns .java files to analyse. For a directory: uses src/main/java if present,
     * and filters to controller/service/dao only. For a file: returns it only if it's .java and in those layers.
     */
    List<Path> findJavaFiles(Path path) throws IOException;
}
