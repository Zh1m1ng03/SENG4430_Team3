package com.team3.io.impl;

import com.team3.io.JavaFileFinder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Default implementation: Maven/Gradle layout (src/main/java), controller/service/dao only.
 */
@Component
public class JavaFileFinderImpl implements JavaFileFinder {

    private static final String SOURCE_MAIN_JAVA = "src/main/java";

    @Override
    public List<Path> findJavaFiles(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            throw new IllegalArgumentException("Path does not exist: " + path);
        }
        if (Files.isRegularFile(path)) {
            if (path.getFileName().toString().endsWith(".java") && isInControllerServiceOrDao(path)) {
                return List.of(path);
            }
            if (!path.getFileName().toString().endsWith(".java")) {
                throw new IllegalArgumentException("Not a Java file: " + path);
            }
            return List.of(); // .java but not in controller/service/dao
        }
        Path searchRoot = path.resolve(SOURCE_MAIN_JAVA);
        if (!Files.isDirectory(searchRoot)) {
            searchRoot = path;
        }
        List<Path> result = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(searchRoot)) {
            walk.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".java"))
                    .filter(this::isInControllerServiceOrDao)
                    .forEach(result::add);
        }
        return result;
    }

    private boolean isInControllerServiceOrDao(Path path) {
        for (Path component : path) {
            String name = component.toString();
            if (name.equals("controller") || name.equals("service") || name.equals("dao")) {
                return true;
            }
        }
        return false;
    }
}
