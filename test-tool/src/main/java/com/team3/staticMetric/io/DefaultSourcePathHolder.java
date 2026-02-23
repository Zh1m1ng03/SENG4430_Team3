package com.team3.staticMetric.io;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Holds the user's default Java source path for static metrics.
 * Inject this where default path is read or updated (e.g. CLI).
 */
public interface DefaultSourcePathHolder {

    Optional<Path> getDefaultPath();

    void setDefaultPath(Path path);

    void clear();
}
