package org.example.cloudstorage.util;

import lombok.experimental.UtilityClass;
import org.example.cloudstorage.model.exception.InvalidPathResourceException;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

@UtilityClass
public class PathUtil {
    private final String ROOT_PATH = "user-%d-files/";

    public static String buildRootPath(Long userId) {
        return ROOT_PATH.formatted(userId);
    }

    public static String buildUserFullPath(Long userId, String path) {
        String normalized = validateAndNormalizePath(path);
        return buildRootPath(userId) + normalized;
    }

    public static boolean isDirectory(String path) {
        return path.endsWith("/");
    }

    public static String getParentPath(String path){
        String normalized = validateAndNormalizePath(path);

        try {
            Path pathObj = Path.of(normalized).normalize();

            if (pathObj.getParent() == null) {
                return "/";
            }
            return pathObj.getParent() + "/";
        } catch (InvalidPathException e) {
            throw new InvalidPathResourceException(e.getMessage());
        }
    }

    public static String getFileName(String path){
        String normalized = validateAndNormalizePath(path);
        try {
            Path pathObj = Path.of(normalized).normalize();

            return pathObj.getFileName().toString();
        } catch (InvalidPathException e) {
            throw new InvalidPathResourceException(e.getMessage());
        }
    }

    private static String validateAndNormalizePath(String path) {
        String pathTrim = path.trim();

        if (pathTrim.isEmpty() || pathTrim.contains("\\") || pathTrim.contains("//")
            || pathTrim.contains("..")
        ) {
            throw new InvalidPathResourceException("Path is invalid");
        }

        return pathTrim;
    }
}
