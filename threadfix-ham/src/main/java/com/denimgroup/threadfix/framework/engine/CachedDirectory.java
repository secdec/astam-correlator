////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2015 Denim Group, Ltd.
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s): Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.framework.engine;

import com.denimgroup.threadfix.framework.util.FilePathUtils;
import com.denimgroup.threadfix.framework.util.PathUtil;
import com.denimgroup.threadfix.logging.SanitizedLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

import static com.denimgroup.threadfix.CollectionUtils.list;
import static com.denimgroup.threadfix.CollectionUtils.map;

// TODO make more error resistant
public class CachedDirectory {

    private File directory;

    @Nonnull
    private final Map<String, Set<String>> fileMap;

    private final SanitizedLogger log = new SanitizedLogger("CachedDirectory");

    public CachedDirectory(@Nonnull File directory) {
        this.directory = directory;
        fileMap = buildMaps(directory);
    }

    @Nonnull
    public File getDirectory() {
        return directory;
    }

    @Nonnull
    public String getDirectoryPath() {
        return FilePathUtils.normalizePath(directory.getAbsolutePath());
    }

    @Nonnull
    private Map<String, Set<String>> buildMaps(@Nonnull File startingFile) {
        Map<String, Set<String>> returnMap = map();

        if (startingFile.isDirectory()) {
            recurseMap(startingFile, returnMap);
        }

        return returnMap;
    }

    @Override
    public String toString() {
        return directory.toString();
    }

    private void recurseMap(@Nonnull File currentDirectory, @Nonnull Map<String, Set<String>> map) {
        if (!currentDirectory.isDirectory() || !currentDirectory.exists()) {
            return;
        }

        File[] files = currentDirectory.listFiles();

        List<File> directories = list();

        if (files != null) {
            for (File file : files) {
                // we want to skip all files / directories that start with .
                if (file != null && file.getName().charAt(0) != '.') {
                    if (file.isFile()) {
                        addToMap(map, file);
                    } else if (file.isDirectory()) {
                        directories.add(file);
                    }
                }
            }
        }

        for (File directory : directories) {
            recurseMap(directory, map);
        }
    }

    private void addToMap(@Nonnull Map<String, Set<String>> map, @Nonnull File file) {
        if (!map.containsKey(file.getName())) {
            map.put(file.getName(), new HashSet<String>());
        }

        String canonicalPath = FilePathUtils.getRelativePath(file, getDirectoryPath());

        if (canonicalPath != null) {
            map.get(file.getName()).add(canonicalPath);
        } else {
            log.warn("Recursive algorithm in CachedDirectory broke out of its starting directory.");
        }
    }

    // TODO we may be able to get better results with some more advanced logic here
    // maybe skip directories like "test", look in specific paths or at least check guesses
    // on the other hand I don't really see this being a bottleneck
    @Nullable
    public File findWebXML() {
        return findBestFile("web.xml", "WEB-INF", "web.xml");
    }

    @Nonnull
    public List<File> findFiles(@Nonnull String ...paths) {
        List<File> files = list();

        for (String path : paths) {
            if (path.contains("*")) {
                // we have to do a wildcard match
                files.addAll(findFilesWithStar(path, false));
            } else {
                // do normal add
                files.add(findBestFile(path));
            }
        }

        return files;
    }

    @Nonnull
    public List<File> findFilesIn(@Nonnull String basePath, @Nonnull String ...paths) {
        File baseFile = new File(basePath);
        if (!baseFile.exists()) {
            baseFile = new File(PathUtil.combine(directory.getAbsolutePath(), basePath));
        }

        String fullBasePath = null;
        if (baseFile.exists()) {
            fullBasePath = baseFile.getAbsolutePath();
        }

        List<File> result = list();
        for (String path : paths) {
            String formattedPath = PathUtil.combine(basePath, path);
            List<File> filesInPath = findFiles(formattedPath);

            if (fullBasePath != null) {
                for (File file : filesInPath) {
                    if (file.getAbsolutePath().startsWith(fullBasePath)) {
                        result.add(file);
                    }
                }
            } else {
                result.addAll(filesInPath);
            }
        }

        return result;
    }

    @Nonnull
    public List<File> findBestFiles(@Nonnull String pathWithStars) {
        List<File> files;

        if (pathWithStars.contains("*")) {
            // we have to do a wildcard match
            files = findFilesWithStar(pathWithStars, true);
        } else {
            // do normal add
            files = Arrays.asList(findBestFile(pathWithStars));
        }

        return files;
    }

    @Nonnull
    private List<File> findFilesWithStar(@Nonnull String path, boolean bestMatch) {
        path = PathUtil.normalizeSeparator(path);
        if (new File(org.apache.commons.io.FilenameUtils.getFullPath(path)).exists()) {
            path = FilePathUtils.getRelativePath(path, this.directory);
        }

        List<File> returnFile = list();
        String[] pathSegments = breakUpPath(path);

        if (pathSegments.length > 0) {
            returnFile = findFilesWithStar(pathSegments[pathSegments.length - 1], bestMatch, pathSegments);
        }

        return returnFile;
    }

    @Nonnull
    private List<File> findFilesWithStar(@Nonnull String fileName, boolean bestMatch, @Nonnull String... pathSegments) {
        List<File> returnFile = list();

        if (fileName.contains("*")) {

            List<String> possibleEntries = list();
            List<String> fullPathParts = list();
            fullPathParts.addAll(Arrays.asList(pathSegments));
            fullPathParts.remove(fullPathParts.size() - 1);

            String[] fullPathPartsArray;

            String[] segments = fileName.split("\\*");
            if (fileName.endsWith("*")) {
                List<String> list = list(segments);
                list.add("");
                segments = list.toArray(new String[list.size()]);
            } else if (fileName.startsWith("*")) {
                fullPathParts.add("");
            }

            for (String segment : segments) {
                if (segment.length() > 0) {
                    fullPathParts.add(segment);
                }
            }

            fullPathPartsArray = fullPathParts.toArray(new String[fullPathParts.size()]);

            for (String key : fileMap.keySet()) {
                if (matches(key, segments)) {
                    possibleEntries.add(key);
                }
            }

            for (String key : possibleEntries) {
                if (bestMatch) {
                    String extension = calculateBestOption(pathSegments, fileMap.get(key));
                    if (extension != null) {
                        File testFile = new File(getDirectoryPath() + extension);
                        if (testFile.exists()) {
                            returnFile.add(testFile);
                        }
                    }
                } else {
                    for (String path : fileMap.get(key)) {
                        if (matches(path, fullPathPartsArray)) {
                            File testFile = new File(getDirectoryPath() + path);
                            if (testFile.exists()) {
                                returnFile.add(testFile);
                            }
                        }
                    }
                }
            }
        }

        return returnFile;
    }

    private boolean matches(@Nonnull String item, @Nonnull String[] segments) {
        int size = segments.length;

        boolean result = false;
        item = item.toLowerCase();

        if (size >= 2) {
            boolean first = item.startsWith(segments[0]);
            boolean last = item.endsWith(segments[size - 1]);

            if (first && last) {
                String progress = item;
                int misses = 0;

                for (String string : segments) {
                    int index = progress.indexOf(string.toLowerCase());
                    if (index != -1) {
                        progress = progress.substring(index);
                    } else {
                        misses = 1;
                        break;
                    }
                }

                result = misses == 0;
            }
        } else if (size == 1) {
            result = item.equals(segments[0].toLowerCase());
        }


        return result;
    }

    /**
     * Find the file on the file system given a static or dynamic file path.
     * This will find the file on the file system with the same name and longest common
     */
    @Nullable
    public File findBestFile(@Nonnull String path) {
        File returnFile = null;
        String[] pathSegments = breakUpPath(path);

        if (pathSegments.length > 0) {
            returnFile = findBestFile(pathSegments[pathSegments.length - 1], pathSegments);
        }

        return returnFile;
    }

    @Nullable
    private File findBestFile(@Nonnull String fileName, @Nonnull String... pathSegments) {
        File returnFile = null;

        if (fileMap.containsKey(fileName) && !fileMap.get(fileName).isEmpty()) {
            String extension = calculateBestOption(pathSegments, fileMap.get(fileName));
            if (extension != null) {
                File testFile = new File(getDirectoryPath() + extension);
                if (testFile.exists()) {
                    returnFile = testFile;
                }
            }
        }

        return returnFile;
    }

    @Nullable
    public String findCanonicalFilePath(@Nonnull String path, @Nonnull String root) {
        String returnString = null;
        String[] pathSegments = breakUpPath(path);

        if (pathSegments.length > 0) {
            returnString = findFilePath(pathSegments[pathSegments.length - 1], pathSegments);

            if (returnString != null) {
                returnString = FilePathUtils.normalizePath(returnString);
            }
        }

        if (returnString != null && returnString.startsWith(root)) {
            returnString = returnString.substring(root.length());
        }

        return returnString;
    }

    @Nullable
    public String findCanonicalFilePath(@Nonnull String path) {
        String returnString = null;
        String[] pathSegments = breakUpPath(path);

        if (pathSegments.length > 0) {
            returnString = findFilePath(pathSegments[pathSegments.length - 1], pathSegments);

            if (returnString != null) {
                returnString = FilePathUtils.normalizePath(returnString);
            }
        }

        if (returnString != null && directory != null && returnString.startsWith(directory.toString())) {
            returnString = returnString.substring(directory.toString().length());
        }

        return returnString;
    }

    @Nullable
    public String findCanonicalFilePath(@Nonnull File file) {
        String filePath = null;

        if (file.exists() && file.getAbsolutePath().startsWith(directory.getAbsolutePath())) {
            filePath = file.getAbsolutePath().substring(directory.getAbsolutePath().length());
        }

        if (filePath != null) {
            return FilePathUtils.normalizePath(filePath);
        } else {
            return null;
        }
    }

    @Nullable
    private String findFilePath(String name, @Nonnull String... pathSegments) {
        String returnPath = null;

        if (fileMap.containsKey(name) && !fileMap.get(name).isEmpty()) {
            returnPath = calculateBestOption(pathSegments, fileMap.get(name));
        }

        return returnPath;
    }

    // score all the items in the set of choices against the given path segments
    @Nullable
    private String calculateBestOption(@Nonnull String[] pathSegments, @Nonnull Set<String> choices) {
        String returnOption = null;

        int highestScore = -1;

        for (String choice : choices) {
            if (choice != null) {
                int choiceScore = calculateScore(breakUpPath(choice), pathSegments);
                if (choiceScore > highestScore) {
                    returnOption = choice;
                    highestScore = choiceScore;
                    if (choiceScore == pathSegments.length) {
                        break;
                    }
                }
            }
        }

        if (returnOption != null) {
            return FilePathUtils.normalizePath(returnOption);
        } else {
            return null;
        }
    }

    // split along / or \ or just return the whole path
    @Nonnull
    private String[] breakUpPath(@Nonnull String choice) {
        choice = PathUtil.normalizeSeparator(choice);
        String[] results;

        if (choice.indexOf('/') != -1) {
            results = choice.split("/");
        } else if (choice.indexOf('\\') != -1) {
            results = choice.split("\\\\");
        } else {
            results = new String[] { choice };
        }

        return results;
    }

    // calculates the length of the common end elements
    private int calculateScore(@Nonnull String[] option, @Nonnull String[] path) {
        int score = 0;

        if (option.length != 0 && path.length != 0) {
            int optionIndex = option.length - 1, pathIndex = path.length - 1;

            while (optionIndex >= 0 && pathIndex >= 0) {
                if (option[optionIndex].equalsIgnoreCase(path[pathIndex])) {
                    score += 1;
                    optionIndex -= 1;
                    pathIndex -= 1;
                } else {
                    break;
                }
            }
        }

        return score;
    }
}
