package forge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A collection of commonly used utility methods.
 */
public final class ForgeUtils
{
    /**
     * A private constructor to prevent instantiation of this class.
     */
    private ForgeUtils() {}

    /**
     * Allows the user of lambda expressions when constructing TimerTasks.
     *
     * @param runnable The runnable to use within the TimerTask.
     * @return A TimerTask
     */
    public static TimerTask timerTaskLambda(Runnable runnable)
    {
        return new TimerTask()
        {
            @Override
            public void run()
            {
                runnable.run();
            }
        };
    }

    /**
     * Returns a list of all files in the given directory.
     *
     * The search is recursive.
     *
     * @param directory The directory to search for files in
     */
    public static List<File> getFileList(String directory) throws IOException
    {
        return getFileList(Paths.get(directory));
    }

    /**
     * Returns a list of all files in the given directory.
     *
     * The search is recursive.
     *
     * @param directory The directory to search for files in
     */
    public static List<File> getFileList(File directory) throws IOException
    {
        return getFileList(directory.toPath());
    }

    /**
     * Returns a list of all files in the given directory.
     *
     * The search is recursive.
     *
     * @param directory The directory to search for files in
     */
    public static List<File> getFileList(Path directory) throws IOException
    {
        try (Stream<Path> stream = Files.walk(directory))
        {
            return stream.sorted(Comparator.reverseOrder())
                         .map(Path::toFile)
                         .filter(Predicate.not(File::isDirectory))
                         .collect(Collectors.toList());
        }
    }

    /**
     * Deletes the given directory.
     *
     * @param directory The directory to delete
     * @throws IOException If deleting the directory fails
     */
    public static void deleteDir(Path directory) throws IOException
    {
        try (Stream<Path> stream = Files.walk(directory))
        {
            stream.sorted(Comparator.reverseOrder())
                  .map(Path::toFile)
                  .forEach(File::delete);
        }
    }

    /**
     * Deletes the given directory.
     *
     * @param directory The directory to delete
     * @throws IOException If deleting the directory fails
     */
    public static void deleteDir(File directory) throws IOException
    {
        try (Stream<Path> stream = Files.walk(directory.toPath()))
        {
            stream.sorted(Comparator.reverseOrder())
                  .map(Path::toFile)
                  .forEach(File::delete);
        }
    }

    /**
     * Deletes the given directory.
     *
     * @param directory The directory to delete
     * @throws IOException If deleting the directory fails
     */
    public static void deleteDir(String directory) throws IOException
    {
        try (Stream<Path> stream = Files.walk(Paths.get(directory)))
        {
            stream.sorted(Comparator.reverseOrder())
                  .map(Path::toFile)
                  .forEach(File::delete);
        }
    }
}
