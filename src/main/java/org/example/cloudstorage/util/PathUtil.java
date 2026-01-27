package org.example.cloudstorage.util;

import lombok.experimental.UtilityClass;
import org.example.cloudstorage.api.ApiErrors;
import org.example.cloudstorage.model.exception.InvalidPathResourceException;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@UtilityClass
public class PathUtil {
    private final String ROOT_PATH = "user-%d-files/";
//    private static final String INVALID_CHARS = "\\#?&<>\"^%[]{}|";


    public static boolean isContainsRootPath(String path, Long userId) {
        return path.contains(ROOT_PATH.formatted(userId));
    }

    public static boolean isRenameAction(String from, String to) {
        return PathUtil.getParentPath(from).equals(PathUtil.getParentPath(to));
    }

    public static String buildRootPath(Long userId) {
        return ROOT_PATH.formatted(userId);
    }

    public static String removeRootPath(String path, Long userId) {
        return path.replace(ROOT_PATH.formatted(userId), "");
    }

    public static String buildUserFullPath(Long userId, String path) {
        String normalized = validateAndNormalizePath(path);
        if (isContainsRootPath(normalized, userId)) return normalized;
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
            throw new InvalidPathResourceException(ApiErrors.INVALID_PATH.getMessage().formatted(path));
        }
    }

    public static String getFileName(String path){
        String normalized = validateAndNormalizePath(path);
        try {
            Path pathObj = Path.of(normalized).normalize();

            return pathObj.getFileName().toString();
        } catch (InvalidPathException e) {
            throw new InvalidPathResourceException(ApiErrors.INVALID_PATH.getMessage().formatted(path));
        }
    }

    private static String validateAndNormalizePath(String path) {
        String pathTrim = path.trim();
        if (pathTrim.isEmpty() || pathTrim.contains("\\") || pathTrim.contains("//")
            || pathTrim.contains("..")
        ) {
            throw new InvalidPathResourceException(ApiErrors.INVALID_PATH.getMessage().formatted(path));
        }

        return pathTrim;
    }

    public List<String> directorySeparator(String path) {
        return Arrays.asList(path.split("/"));
    }
}
