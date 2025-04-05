package forge;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
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
     * Sets the system clipboard to the given text.
     *
     * @param text The text to set the clipboard to
     */
    public static void setClipboard(String text)
    {
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }

    /**
     * Returns the MD5 checksum of the given file.
     *
     * @param file The file to get the checksum of
     * @param counter An optional counter to update with the number of bytes read - pass null to not use
     * @return An MD5 checksum String
     * @throws Exception If an error occurs getting the checksum
     */
    public static String md5Sum(String file, AtomicLong counter) throws Exception
    {
        return md5Sum(new File(file), counter);
    }

    /**
     * Returns the MD5 checksum of the given file.
     *
     * @param path The file to get the checksum of
     * @param counter An optional counter to update with the number of bytes read - pass null to not use
     * @return An MD5 checksum String
     * @throws Exception If an error occurs getting the checksum
     */
    public static String md5Sum(Path path, AtomicLong counter) throws Exception
    {
        return md5Sum(path.toFile(), counter);
    }

    /**
     * Returns the MD5 checksum of the given file.
     *
     * @param file The file to get the checksum of
     * @param counter An optional counter to update with the number of bytes read - pass null to not use
     * @return An MD5 checksum String
     * @throws Exception If an error occurs getting the checksum
     */
    public static String md5Sum(File file, AtomicLong counter) throws Exception
    {
        try (FileInputStream in = new FileInputStream(file))
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] byteArray = new byte[1024];

            int bytesCount;
            while ((bytesCount = in.read(byteArray)) != -1)
            {
                if (counter != null)
                {
                    counter.addAndGet(bytesCount);
                }

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
     * Converts the given number of bytes to a human-readable format.
     *
     * @param bytes The number of bytes to convert
     * @param binary True to use binary format (2^10), false to use decimal format (10^3)
     * @return A human-readable String representation of the given number of bytes
     */
    public static String convertBytes(double bytes, boolean binary)
    {
        int base = binary ? 1024 : 1000;
        String[] units = binary ? new String[]{"Bytes", "KiB", "MiB", "GiB", "TiB", "PiB"} :
                                  new String[]{"Bytes", "KB", "MB", "GB", "TB", "PB"};
        int unitIndex = 0;
        double size = bytes;

        while (size >= base && unitIndex < units.length - 1)
        {
            size /= base;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }


    /**
     * Converts a time in nanoseconds to a human-readable format.
     *
     * If the time given is less than or equal to 0, an empty String is returned.
     *
     * @param nanoTime A time in nanoseconds
     * @return A human-readable String representation of the time
     */
    public static String timeToStr(long nanoTime)
    {
        if (nanoTime <= 0) return "";

        //determine a suitable TimeUnit to use
        TimeUnit timeUnit;
        if (nanoTime < 1_000) //less than 1 microsecond
        {
            timeUnit = TimeUnit.NANOSECONDS;
        }
        else if (nanoTime < 1_000_000) //less than 1 millisecond
        {
            timeUnit = TimeUnit.MICROSECONDS;
        }
        else if (nanoTime < 1_000_000_000) //less than 1 second
        {
            timeUnit = TimeUnit.MILLISECONDS;
        }
        else //1 second or more
        {
            timeUnit = TimeUnit.SECONDS;
        }

        String result = "";

        //calculate the time elapsed
        switch (timeUnit)
        {
            case NANOSECONDS, MICROSECONDS, MILLISECONDS ->
            {
                result += timeUnit.convert(nanoTime, TimeUnit.NANOSECONDS) + " " + timeUnit.toString().toLowerCase();
            }
            case SECONDS, MINUTES, HOURS, DAYS ->
            {
                long days = TimeUnit.DAYS.convert(nanoTime, TimeUnit.NANOSECONDS);
                long hours = TimeUnit.HOURS.convert(nanoTime % TimeUnit.NANOSECONDS.convert(1, TimeUnit.DAYS), TimeUnit.NANOSECONDS);
                long minutes = TimeUnit.MINUTES.convert(nanoTime % TimeUnit.NANOSECONDS.convert(1, TimeUnit.HOURS), TimeUnit.NANOSECONDS);
                long seconds = TimeUnit.SECONDS.convert(nanoTime % TimeUnit.NANOSECONDS.convert(1, TimeUnit.MINUTES), TimeUnit.NANOSECONDS);

                if (days > 0) result += days + " days ";
                if (days > 0 || hours > 0) result += hours + " hours ";
                if (days > 0 || hours > 0 || minutes > 0) result += minutes + " minutes ";

                //use fractional seconds for closer comparisons
                long totalSeconds = TimeUnit.SECONDS.convert(nanoTime, TimeUnit.NANOSECONDS);
                if (totalSeconds < 50)
                {
                    double fractionalSeconds = nanoTime % TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS) / 1e9;
                    result += String.format("%.2f seconds", seconds + fractionalSeconds);
                }
                else
                {
                    result += seconds + " seconds";
                }
            }
        }

        return result;
    }

    /**
     * Allows the user of lambda expressions when constructing TimerTasks.
     *
     * @param runnable The runnable to use within the TimerTask.
     * @return A TimerTask
     */
    public static TimerTask timerTask(Runnable runnable)
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
    public static List<Path> getSubdirectories(String directory) throws IOException
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
    public static List<Path> getSubdirectories(File directory) throws IOException
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
    public static List<Path> getSubdirectories(Path directory) throws IOException
    {
        try (Stream<Path> stream = Files.walk(directory))
        {
            return stream.sorted(Comparator.reverseOrder())
                         .filter(Files::isDirectory)
                         .collect(Collectors.toList());
        }
    }

    /**
     * Returns a list of all directories in the root of the given directory.
     * The search is not recursive and files are excluded.
     *
     * @param directory The directory to search for immediate subdirectories
     * @return A list of all subdirectories in the root of the given directory
     */
    public static List<Path> getRootSubdirectories(String directory)
    {
        return getRootSubdirectories(new File(directory));
    }

    /**
     * Returns a list of all directories in the root of the given directory.
     * The search is not recursive and files are excluded.
     *
     * @param directory The directory to search for immediate subdirectories
     * @return A list of all subdirectories in the root of the given directory
     */
    public static List<Path> getRootSubdirectories(Path directory)
    {
        return getRootSubdirectories(directory.toFile());
    }

    /**
     * Returns a list of all directories in the root of the given directory.
     * The search is not recursive and files are excluded.
     *
     * @param directory The directory to search for immediate subdirectories
     * @return A list of all subdirectories in the root of the given directory
     */
    public static List<Path> getRootSubdirectories(File directory)
    {
        return Arrays.stream(directory.listFiles())
                     .filter(File::isDirectory)
                     .map(File::toPath)
                     .collect(Collectors.toList());
    }

    /**
     * Returns a list of all files in the given directory.
     *
     * The search is recursive and directories are excluded.
     *
     * @param directory The directory to search for files in
     * @return A list of all files in the given directory
     * @throws IOException If an error occurs searching the given directory
     */
    public static List<Path> getFiles(String directory) throws IOException
    {
        return getFiles(Paths.get(directory));
    }

    /**
     * Returns a list of all files in the given directory.
     *
     * The search is recursive and directories are excluded.
     *
     * @param directory The directory to search for files in
     * @return A list of all files in the given directory
     * @throws IOException If an error occurs searching the given directory
     */
    public static List<Path> getFiles(File directory) throws IOException
    {
        return getFiles(directory.toPath());
    }

    /**
     * Returns a list of all files in the given directory.
     *
     * The search is recursive and directories are excluded.
     *
     * @param directory The directory to search for files in
     * @return A list of all files in the given directory
     * @throws IOException If an error occurs searching the given directory
     */
    public static List<Path> getFiles(Path directory) throws IOException
    {
        List<Path> files = new ArrayList<>();
        Files.walkFileTree(directory, new FilteringFileVisitor(files, Predicate.not(Files::isDirectory)));
        return files;
    }

    /**
     * Returns a list of all files in the root of the given directory.
     * The search is not recursive and directories are excluded.
     *
     * @param directory The directory to search for immediate files in
     * @return A list of all files in the root of the given directory
     */
    public static List<Path> getRootFiles(String directory)
    {
        return getRootFiles(new File(directory));
    }

    /**
     * Returns a list of all files in the root of the given directory.
     * The search is not recursive and directories are excluded.
     *
     * @param directory The directory to search for immediate files in
     * @return A list of all files in the root of the given directory
     */
    public static List<Path> getRootFiles(Path directory)
    {
        return getRootFiles(directory.toFile());
    }

    /**
     * Returns a list of all files in the root of the given directory.
     * The search is not recursive and directories are excluded.
     *
     * @param directory The directory to search for immediate files in
     * @return A list of all files in the root of the given directory
     */
    public static List<Path> getRootFiles(File directory)
    {
        return Arrays.stream(directory.listFiles())
                     .filter(File::isFile)
                     .map(File::toPath)
                     .collect(Collectors.toList());
    }

    /**
     * Returns a list of all files and directories in the given directory.
     *
     * The search is recursive.
     *
     * @param directory The directory to search for files in
     * @throws IOException If an error occurs searching the given directory
     */
    public static List<Path> getAllFiles(String directory) throws IOException
    {
        return getAllFiles(Paths.get(directory));
    }

    /**
     * Returns a list of all files and directories in the given directory.
     *
     * The search is recursive.
     *
     * @param directory The directory to search for files in
     * @throws IOException If an error occurs searching the given directory
     */
    public static List<Path> getAllFiles(File directory) throws IOException
    {
        return getAllFiles(directory.toPath());
    }

    /**
     * Returns a list of all files and directories in the given directory.
     *
     * The search is recursive.
     *
     * @param directory The directory to search for files in
     * @throws IOException If an error occurs searching the given directory
     */
    public static List<Path> getAllFiles(Path directory) throws IOException
    {
        List<Path> files = new ArrayList<>();
        Files.walkFileTree(directory, new FilteringFileVisitor(files));
        files.sort(Comparator.reverseOrder());
        return files;
    }

    /**
     * Returns a list of all files and directories in the root of the given directory.
     * The search is not recursive.
     *
     * @param directory The directory to search for files in
     */
    public static List<Path> getAllRootFiles(String directory)
    {
        return getAllRootFiles(new File(directory));
    }

    /**
     * Returns a list of all files and directories in the root of the given directory.
     * The search is not recursive.
     *
     * @param directory The directory to search for files in
     */
    public static List<Path> getAllRootFiles(Path directory)
    {
        return getAllRootFiles(directory.toFile());
    }

    /**
     * Returns a list of all files and directories in the root of the given directory.
     * The search is not recursive.
     *
     * @param directory The directory to search for files in
     */
    public static List<Path> getAllRootFiles(File directory)
    {
        return Arrays.stream(directory.listFiles())
                     .map(File::toPath)
                     .collect(Collectors.toList());
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
    public static void deleteDir(String directory) throws IOException
    {
        deleteDir(Paths.get(directory));
    }

    /**
     * Deletes the given directory.
     *
     * @param directory The directory to delete
     * @throws IOException If deleting the directory fails
     */
    public static void deleteDir(File directory) throws IOException
    {
        deleteDir(directory.toPath());
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
     * A FileVisitor that filters files based on the given predicates.
     *
     * This class additionally handles AccessDeniedExceptions by printing an error message
     * and skipping the subtree instead of throwing an exception, as Files.walk() does.
     */
    private static final class FilteringFileVisitor extends SimpleFileVisitor<Path>
    {
        private final List<Path> files; //a list of the files visited
        private final List<Predicate<Path>> filters; //a list of filters to test

        /**
         * Creates a new FilteringFileVisitor.
         *
         * @param files To fill with the files visited
         * @param filters To filter the files visited - Paths that do not pass all filters will be excluded
         */
        @SafeVarargs
        public FilteringFileVisitor(List<Path> files, Predicate<Path>... filters)
        {
            this.files = files;
            this.filters = List.of(filters);
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
        {
            //only add files that pass all filters
            if (this.filters.stream().allMatch(filter -> filter.test(file)))
            {
                this.files.add(file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException
        {
            //handle AccessDeniedExceptions by printing an error message and skipping the subtree
            if (e instanceof AccessDeniedException)
            {
                System.err.println("Access denied to file: " + file.toString());
                return FileVisitResult.SKIP_SUBTREE;
            }
            throw e;
        }
    }
}
