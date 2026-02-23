package com.team3.staticMetric.io.impl;

import com.team3.staticMetric.io.DefaultSourcePathHolder;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * In-memory implementation of the default source path holder.
 */
@Component
public class DefaultSourcePathHolderImpl implements DefaultSourcePathHolder {

    private final AtomicReference<Path> defaultPath = new AtomicReference<>(null);

    @Override
    public Optional<Path> getDefaultPath() {
        return Optional.ofNullable(defaultPath.get());
    }

    @Override
    public void setDefaultPath(Path path) {
        this.defaultPath.set(path);
    }

    @Override
    public void clear() {
        this.defaultPath.set(null);
    }
}
