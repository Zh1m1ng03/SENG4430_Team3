package com.team3.analyser;

/**
 * Input for dynamic metrics (e.g. execution time): what to run and how.
 * Extend with more fields (args, timeout, etc.) as needed.
 */
public record RunConfig(
        String mainClass,
        String methodName
) {
    public static RunConfig of(String mainClass, String methodName) {
        return new RunConfig(mainClass, methodName);
    }
}
