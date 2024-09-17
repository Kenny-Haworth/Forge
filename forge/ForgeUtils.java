package forge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
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
     * Returns the MD5 checksum of the given file.
     *
     * @param file The file to get the checksum of
     * @return An MD5 checksum String
     * @throws Exception If an error occurs getting the checksum
     */
    public static String getMD5Sum(String file) throws Exception
    {
        try (FileInputStream in = new FileInputStream(file))
        {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] byteArray = new byte[1024];
            int bytesCount;

            while ((bytesCount = in.read(byteArray)) != -1)
            {
                md.update(byteArray, 0, bytesCount);
            }

            StringBuilder builder = new StringBuilder();
            for (byte bite : md.digest())
            {
                builder.append(String.format("%02x", bite));
            }

            return builder.toString();
        }
    }

    /**
     * Returns the MD5 checksum of the given file.
     *
     * @param file The file to get the checksum of
     * @return An MD5 checksum String
     * @throws Exception If an error occurs getting the checksum
     */
    public static String getMD5Sum(File file) throws Exception
    {
        return getMD5Sum(file.getAbsolutePath());
    }

    /**
     * Returns the MD5 checksum of the given file.
     *
     * @param file The file to get the checksum of
     * @return An MD5 checksum String
     * @throws Exception If an error occurs getting the checksum
     */
    public static String getMD5Sum(Path file) throws Exception
    {
        return getMD5Sum(file.toString());
    }

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
     * Returns a list of all subdirectories in the given directory.
     *
     * The search is recursive and files are excluded.
     *
     * @param directory The directory to search for subdirectories
     * @return A list of all subdirectories in the given directory
     * @throws IOException If getting the subdirectories fails
     */
    public static List<File> getSubdirectories(String directory) throws IOException
    {
        return getSubdirectories(Paths.get(directory));
    }

    /**
     * Returns a list of all subdirectories in the given directory.
     *
     * The search is recursive and files are excluded.
     *
     * @param directory The directory to search for subdirectories
     * @return A list of all subdirectories in the given directory
     * @throws IOException If getting the subdirectories fails
     */
    public static List<File> getSubdirectories(File directory) throws IOException
    {
        return getSubdirectories(directory.toPath());
    }

    /**
     * Returns a list of all subdirectories in the given directory.
     *
     * The search is recursive and files are excluded.
     *
     * @param directory The directory to search for subdirectories
     * @return A list of all subdirectories in the given directory
     * @throws IOException If getting the subdirectories fails
     */
    public static List<File> getSubdirectories(Path directory) throws IOException
    {
        try (Stream<Path> stream = Files.walk(directory))
        {
            return stream.sorted(Comparator.reverseOrder())
                         .map(Path::toFile)
                         .filter(File::isDirectory)
                         .collect(Collectors.toList());
        }
    }

    /**
     * Returns a list of all files in the given directory.
     *
     * The search is recursive and directories are excluded.
     *
     * @param directory The directory to search for files in
     * @throws IOException If an error occurs searching the given directory
     */
    public static List<File> getFileList(String directory) throws IOException
    {
        return getFileList(Paths.get(directory));
    }

    /**
     * Returns a list of all files in the given directory.
     *
     * The search is recursive and directories are excluded.
     *
     * @param directory The directory to search for files in
     * @throws IOException If an error occurs searching the given directory
     */
    public static List<File> getFileList(File directory) throws IOException
    {
        return getFileList(directory.toPath());
    }

    /**
     * Returns a list of all files in the given directory.
     *
     * The search is recursive and directories are excluded.
     *
     * @param directory The directory to search for files in
     * @throws IOException If an error occurs searching the given directory
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
     * Returns a list of all files and directories in the given directory.
     *
     * The search is recursive.
     *
     * @param directory The directory to search for files in
     * @throws IOException If an error occurs searching the given directory
     */
    public static List<File> getAllFilesList(String directory) throws IOException
    {
        return getAllFilesList(Paths.get(directory));
    }

    /**
     * Returns a list of all files and directories in the given directory.
     *
     * The search is recursive.
     *
     * @param directory The directory to search for files in
     * @throws IOException If an error occurs searching the given directory
     */
    public static List<File> getAllFilesList(File directory) throws IOException
    {
        return getAllFilesList(directory.toPath());
    }

    /**
     * Returns a list of all files and directories in the given directory.
     *
     * The search is recursive.
     *
     * @param directory The directory to search for files in
     * @throws IOException If an error occurs searching the given directory
     */
    public static List<File> getAllFilesList(Path directory) throws IOException
    {
        try (Stream<Path> stream = Files.walk(directory))
        {
            return stream.sorted(Comparator.reverseOrder())
                         .map(Path::toFile)
                         .collect(Collectors.toList());
        }
    }

    /**
     * Returns true if the given directory is empty, false otherwise.
     *
     * @param directory The directory to check for emptiness
     * @return True if the given directory is empty, false otherwise
     * @throws IOException If an error occurs checking the directory
     */
    public static boolean isDirectoryEmpty(String directory) throws IOException
    {
        return isDirectoryEmpty(Paths.get(directory));
    }

    /**
     * Returns true if the given directory is empty, false otherwise.
     *
     * @param directory The directory to check for emptiness
     * @return True if the given directory is empty, false otherwise
     * @throws IOException If an error occurs checking the directory
     */
    public static boolean isDirectoryEmpty(File directory) throws IOException
    {
        return isDirectoryEmpty(directory.toPath());
    }

    /**
     * Returns true if the given directory is empty, false otherwise.
     *
     * @param directory The directory to check for emptiness
     * @return True if the given directory is empty, false otherwise
     * @throws IOException If an error occurs checking the directory
     */
    public static boolean isDirectoryEmpty(Path directory) throws IOException
    {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory))
        {
            return !stream.iterator().hasNext();
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
