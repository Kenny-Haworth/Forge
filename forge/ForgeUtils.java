package forge;

import static forge.Logger.log;
import static forge.Logger.logError;
import static forge.Logger.logWarning;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.imageio.ImageIO;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinUser;

/**
 * A collection of commonly used utility methods.
 */
public final class ForgeUtils
{
    /**
     * A private constructor to prevent non-static use of this class.
     */
    private ForgeUtils() {}

    //ensures deepest files and directories are first
    public static final Comparator<Path> LONGEST_PATHS_FIRST = (item1, item2) ->
    {
        int length = item2.toString().length() - item1.toString().length();

        //compare lexicographically if length is the same to ensure a stable sort
        if (length == 0)
        {
            return item2.toString().compareTo(item1.toString());
        }

        return length;
    };

    /**
     * Converts a temperature from celsius to fahrenheit.
     *
     * @param celsius The temperature in celsius
     * @return A temperature in fahrenheit
     */
    public static double celsiusToFahrenheit(double celsius)
    {
        return celsius * 1.8 + 32;
    }

    /**
     * Converts a temperature from fahrenheit to celsius.
     *
     * @param fahrenheit The temperature in fahrenheit
     * @return A temperature in celsius
     */
    public static double fahrenheitToCelsius(double fahrenheit)
    {
        return (fahrenheit - 32) / 1.8;
    }

    /**
     * Loads image at the given path and scales it to the given size.
     *
     * @param iconPath The path to load the icon from
     * @param width The width to set the image
     * @param height The height to set the image
     * @return An Image set to the given size
     * @throws IOException If loading the image fails
     */
    public static Image loadImage(String iconPath, int width, int height) throws IOException
    {
        return ImageIO.read(new File(iconPath)).getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    /**
     * Given a CSV file, aligns each column by appending whitespace.
     *
     * The given file will be overwritten.
     *
     * @param csvPath The path to the CSV file
     * @throws IOException If an error occurs reading from or writing to the CSV
     */
    public static void alignCsv(Path csvPath) throws IOException
    {
        //read the CSV into a 2D grid
        List<String> csv = Files.readAllLines(csvPath);
        String[][] cells = new String[csv.size()][];

        for (int i = 0; i < csv.size(); i++)
        {
            cells[i] = csv.get(i).split(",");
        }

        //process columns sequentially
        for (int col = 0 ; col < cells[0].length; col++)
        {
            //determine the longest line in each column
            int longestLine = Integer.MIN_VALUE;
            for (int row = 0; row < cells.length; row++)
            {
                longestLine = Math.max(longestLine, cells[row][col].length());
            }

            //pad spaces in each cell in this column to reach the longest line
            for (int row = 0; row < cells.length; row++)
            {
                cells[row][col] += " ".repeat(longestLine - cells[row][col].length());
            }
        }

        //overwrite the file
        try (FileWriter writer = new FileWriter(csvPath.toFile()))
        {
            for (int row = 0; row < cells.length; row++)
            {
                writer.write(Arrays.stream(cells[row]).collect(Collectors.joining(", ")) + "\n");
            }
        }
    }

    /**
     * Sets the global default font for the program.
     *
     * @param font A default font to set for the program
     */
    public static void setGlobalFont(FontUIResource font)
    {
        StreamSupport.stream(Spliterators.spliteratorUnknownSize(UIManager.getDefaults().keys().asIterator(), Spliterator.ORDERED), false)
                     .filter(key -> UIManager.get(key) instanceof FontUIResource)
                     .forEach(key -> UIManager.put(key, font));
    }

    /**
     * Centers the given component on the given monitor.
     *
     * @param component The component to center
     * @param device The monitor to center the component on
     */
    public static void centerComponent(Component component, GraphicsDevice device)
    {
        Rectangle bounds = device.getDefaultConfiguration().getBounds();
        Dimension size = component.getSize();
        component.setLocation(bounds.x + (bounds.width - size.width) / 2,
                              bounds.y + (bounds.height - size.height) / 2);
    }

    /**
     * Creates a new TimerTask using the given Runnable.
     *
     * This allows construction of TimerTasks using lambda expressions.
     *
     * @param runnable The runnable to use for the TimerTask's run() method
     * @return A TimerTask
     */
    public static TimerTask timer(Runnable runnable)
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
     * Launches the given program.
     *
     * This method ensures the executable exists before attempting to launch it.
     *
     * @param fullPath The full path to the program
     * @param allowMultipleInstances False to only launch the program if it is not already running,
     *                               true to allow multiple instances
     */
    public static void launchProgram(String fullPath, boolean allowMultipleInstances)
    {
        //ensure the executable exists
        if (!Files.exists(Paths.get(fullPath)))
        {
            logWarning("The executable \"" + fullPath + "\" does not exist, so the program cannot be started.");
            return;
        }

        String executable = fullPath.substring(fullPath.lastIndexOf('/') + 1);

        //the program does not need to be started because it is already running
        if (!allowMultipleInstances && programRunning(executable))
        {
            log(executable + " is already running");
            return;
        }

        //get the path without the executable in it
        String path = fullPath.substring(0, fullPath.lastIndexOf('/'));

        //launch the program
        try
        {
            Process process = new ProcessBuilder("cmd", "/c", "start /D \"" + path + "\" " + executable)
                              .redirectErrorStream(true).start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                List<String> lines = reader.lines().collect(Collectors.toList());
                if (!lines.isEmpty())
                {
                    logWarning("Unexpected output attempting to startup " + executable + ": " +
                                lines.stream().collect(Collectors.joining("\n")));
                }
                else
                {
                    String name = executable.split("\\.")[0];
                    log(name + " started successfully");
                }

                int exitCode = process.waitFor();
                if (exitCode != 0)
                {
                    logError(executable + " exited with error code " + exitCode);
                }
            }
        }
        catch (IOException | InterruptedException e)
        {
            logError("Unable to start " + executable + " automatically", e);
        }
    }

    /**
     * Starts a Task Scheduler task to launch a program.
     *
     * This method is especially useful to launch programs as another user, something Windows does not have a direct command for.
     * This can only be done if this Java program was launched with administrative privileges.
     *
     * @param taskName The name of the Task Scheduler task to run
     * @param executable The executable name (unused if allowMultipleInstances is true)
     * @param allowMultipleInstances False to only launch the program if it is not already running,
     *                               true to allow multiple instances
     */
    public static void runTaskSchedulerTask(String taskName, String executable, boolean allowMultipleInstances)
    {
        //the task does not need to be run because the program is already running
        if (!allowMultipleInstances && programRunning(executable))
        {
            log(executable + " is already running");
            return;
        }

        //run the task
        try
        {
            Process process = new ProcessBuilder("cmd", "/c", "schtasks /run /tn " + taskName)
                              .redirectErrorStream(true).start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                List<String> lines = reader.lines().collect(Collectors.toList());

                //the task started successfully
                if (lines.size() == 1 &&
                    lines.get(0).equals("SUCCESS: Attempted to run the scheduled task \"" + taskName + "\"."))
                {
                    String name = executable.split("\\.")[0];
                    log(name + " started successfully");
                }
                //something went wrong
                else
                {
                    logWarning("Unexpected output attempting to run " + taskName + " via Task Scheduler: " +
                                lines.stream().collect(Collectors.joining("\n")));
                }

                int exitCode = process.waitFor();
                if (exitCode != 0)
                {
                    logError("The Task Scheduler task " + taskName + " exited with error code " + exitCode);
                }
            }
        }
        catch (IOException | InterruptedException e)
        {
            logError("Unable to start Task Scheduler task " + taskName + " automatically", e);
        }
    }

    /**
     * Determines if any instances of the given executable are already running.
     *
     * If an exception occurs, this method will log it and return true.
     *
     * @param executable The executable to check for instances of
     * @return True if the given executable is already running, false otherwise
     */
    private static boolean programRunning(String executable)
    {
        boolean programRunning = true; //assume the program is running by default

        try
        {
            Process process = new ProcessBuilder("cmd", "/c", "tasklist /fi \"imagename eq " + executable + "\"")
                              .redirectErrorStream(true).start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    //this line indicates no instances of the program are currently running
                    if ("INFO: No tasks are running which match the specified criteria.".equals(line))
                    {
                        programRunning = false;
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode != 0)
                {
                    logError("The command to check for any instances of " + executable + " exited with error code " + exitCode);
                }
            }
        }
        catch (IOException | InterruptedException e)
        {
            logError("Unable to determine if " + executable + " is currently running", e);
        }

        return programRunning;
    }

    /**
     * Registers a kill switch that will exit the application when the escape key is pressed.
     *
     * The program will exit regardless of what application is currently focused.
     * This method only works on Windows.
     */
    public static void registerEscapeKillSwitch()
    {
        Thread thread = new Thread(() ->
        {
            WinUser.MSG msg = new WinUser.MSG();
            User32.INSTANCE.RegisterHotKey(null, 1, 0, 0x1B); //escape key
            while (User32.INSTANCE.GetMessage(msg, null, 0, 0) != 0)
            {
                if (msg.message == WinUser.WM_HOTKEY)
                {
                    System.out.println("Escape key pressed, exiting!");
                    System.exit(0);
                }
            }
        }, "Escape Kill Switch");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Sets the graphic's font to the largest size possible while still fitting the given text into the given pixel width.
     *
     * @param g2d The graphics context to set the font of
     * @param text The text that will be displayed. This need not be the exact text to be displayed, but can, for example, be text
     *             of the largest expected length for a component to keep the text a consistent size as the length changes.
     * @param pixelWidth The number of pixels wide that is available to draw the text
     */
    public static void setFontFromWidth(Graphics2D g2d, String text, int pixelWidth)
    {
        Font font = g2d.getFont();
        Rectangle2D rect = g2d.getFontMetrics(font).getStringBounds(text, g2d);
        g2d.setFont(font.deriveFont((float)((double)font.getSize2D() * pixelWidth/rect.getWidth())));
    }

    /**
     * Sets the graphic's font to the largest size possible while still fitting the given text into the given pixel height.
     *
     * @param g2d The graphics context to set the font of
     * @param text The text that will be displayed. This need not be the exact text to be displayed, but can, for example, be text
     *             of the largest expected length for a component to keep the text a consistent size as the length changes.
     * @param pixelHeight The number of pixels high that is available to draw the text
     */
    public static void setFontFromHeight(Graphics2D g2d, String text, int pixelHeight)
    {
        Font font = g2d.getFont();
        Rectangle2D rect = g2d.getFontMetrics(font).getStringBounds(text, g2d);
        g2d.setFont(font.deriveFont((float)((double)font.getSize2D() * pixelHeight/rect.getHeight())));
    }

    /**
     * Sets the graphic's font to the largest size possible while still fitting the given text into the given pixel width and height.
     *
     * @param g2d The graphics context to set the font of
     * @param text The text that will be displayed. This need not be the exact text to be displayed, but can, for example, be text
     *             of the largest expected length for a component to keep the text a consistent size as the length changes.
     * @param pixelWidth The number of pixels wide that is available to draw the text
     * @param pixelHeight The number of pixels high that is available to draw the text
     */
    public static void setFontFromWidthAndHeight(Graphics2D g2d, String text, int pixelWidth, int pixelHeight)
    {
        Font font = g2d.getFont();
        Rectangle2D rect = g2d.getFontMetrics(font).getStringBounds(text, g2d);
        g2d.setFont(font.deriveFont((float)Math.min(((double)font.getSize2D() * pixelWidth/rect.getWidth()),
                                                    ((double)font.getSize2D() * pixelHeight/rect.getHeight()))));
    }

    /**
     * Safely casts the given Object to a Map.
     *
     * @param <S> The key type
     * @param <T> The value type
     * @param object The object to cast to a Map
     * @param keyType A class instance of the key type
     * @param valueType A class instance of the value type
     * @return A Map of the types specified
     */
    public static <S, T> Map<S, T> objToMap(Object object, Class<S> keyType, Class<T> valueType)
    {
        return ((Map<?, ?>)object).entrySet()
                                  .stream()
                                  .collect(Collectors.toMap(
                                                e -> keyType.cast(e.getKey()),
                                                e -> valueType.cast(e.getValue())));
    }

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
     * Returns a timestamp for the current time.
     * The formatting is yyyyMMdd_HHmmss (e.g. 20250801_205232).
     *
     * @return A timestamp for the current time
     */
    public static String getTimeStamp()
    {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    /**
     * Returns the MD5 checksum of the given file.
     *
     * @param file The file to get the checksum of
     * @param counter An optional counter to update with the number of bytes checksummed. Pass null to not use.
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
     * @param counter An optional counter to update with the number of bytes checksummed. Pass null to not use.
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
     * @param counter An optional counter to update with the number of bytes checksummed. Pass null to not use.
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
            { //NOSONAR
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
     * Compares the two given colors and returns true if they are similar.
     *
     * @param color1 The first color to compare
     * @param color2 The second color to compare
     * @param maxAllowedDifference The max allowed value difference between red, green, and blue channels
     * @return True if the given colors are similar
     */
    public static boolean colorIsSimilar(Color color1, Color color2, int maxAllowedDifference)
    {
        return Math.abs(color1.getRed() - color2.getRed()) <= maxAllowedDifference &&
               Math.abs(color1.getGreen() - color2.getGreen()) <= maxAllowedDifference &&
               Math.abs(color1.getBlue() - color2.getBlue()) <= maxAllowedDifference;
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
        List<Path> files = new ArrayList<>();
        Files.walkFileTree(directory, new FilteringFileVisitor(files, Files::isDirectory));
        return files;
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
     * Determines if the given file or directory lives at the root of the given directory.
     *
     * If the given file or directory is equal to the given directory, this method will return false.
     *
     * @param directory The directory to check the file/directory against
     * @param file The file or directory to test if it lives at the root level of the given directory
     * @return True if the file or directory lives at the root of the given directory, false otherwise
     */
    public static boolean isRootLevel(Path directory, Path file)
    {
        String pathStr = file.toString();
        String rootPath = directory.toString() + File.separator;
        return pathStr.startsWith(rootPath) && !pathStr.substring(rootPath.length()).contains(File.separator);
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
        files.sort(LONGEST_PATHS_FIRST);
        return files;
    }

    /**
     * Fills lists of all files and directories in the given directory.
     *
     * The search is recursive.
     *
     * @param directory The directory to search within
     * @param files A list of all files in the given directories
     * @param directories A list of all subdirectories in the given directory
     * @throws IOException If an error occurs searching the given directory
     */
    public static void getAllFiles(String directory, List<Path> files, List<Path> directories) throws IOException
    {
        getAllFiles(Paths.get(directory), files, directories);
    }

    /**
     * Fills lists of all files and directories in the given directory.
     *
     * The search is recursive.
     *
     * @param directory The directory to search within
     * @param files A list of all files in the given directories
     * @param directories A list of all subdirectories in the given directory
     * @throws IOException If an error occurs searching the given directory
     */
    public static void getAllFiles(File directory, List<Path> files, List<Path> directories) throws IOException
    {
        getAllFiles(directory.toPath(), files, directories);
    }

    /**
     * Fills lists of all files and directories in the given directory.
     *
     * The search is recursive.
     *
     * @param directory The directory to search within
     * @param files A list of all files in the given directories
     * @param directories A list of all subdirectories in the given directory
     * @throws IOException If an error occurs searching the given directory
     */
    public static void getAllFiles(Path directory, List<Path> files, List<Path> directories) throws IOException
    {
        Files.walkFileTree(directory, new FilteringFileVisitor(files, directories));
        directories.sort(LONGEST_PATHS_FIRST);
        files.sort(LONGEST_PATHS_FIRST);
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
     * A FileVisitor that filters directories and files based on the given predicates.
     *
     * This class additionally handles AccessDeniedExceptions by printing an error message
     * and skipping the subtree instead of throwing an exception, as Files.walk() does.
     */
    private static final class FilteringFileVisitor extends SimpleFileVisitor<Path>
    {
        private final List<Path> directories; //a list of the directories visited
        private final List<Path> files; //a list of the files (and potentially directories) visited
        private final List<Predicate<Path>> filters; //a list of filters to test

        /**
         * Creates a new FilteringFileVisitor.
         *
         * @param files To fill with the files and directories visited
         * @param filters To filter the files visited - Paths that do not pass all filters will be excluded
         */
        @SafeVarargs
        public FilteringFileVisitor(List<Path> files, Predicate<Path>... filters)
        {
            this.files = files;
            this.filters = List.of(filters);
            this.directories = null;
        }

        /**
         * Creates a new FilteringFileVisitor.
         *
         * @param files To fill with the files visited
         * @param directories To fill with the directories visited
         */
        public FilteringFileVisitor(List<Path> files, List<Path> directories)
        {
            this.files = files;
            this.directories = directories;
            this.filters = Collections.emptyList();
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
        {
            //skip special Windows paths and symbolic links
            if (dir.toString().contains("$Recycle.Bin") ||
                dir.toString().contains("System Volume Information") ||
                Files.isSymbolicLink(dir))
            {
                return FileVisitResult.SKIP_SUBTREE;
            }
            //add this directory if it passes all filters
            else if (this.filters.stream().allMatch(filter -> filter.test(dir)))
            {
                //place directories in "files"
                if (this.directories == null)
                {
                    this.files.add(dir);
                }
                //place directories in the dedicated directories list
                else
                {
                    this.directories.add(dir);
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
        {
            //only add files that are readable, pass all filters, and are not symbolic links
            if (Files.isReadable(file) &&
                this.filters.stream().allMatch(filter -> filter.test(file)) &&
                !Files.isSymbolicLink(file))
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
                System.err.println("Access denied to file: " + file);
                return FileVisitResult.SKIP_SUBTREE;
            }

            //handle non-existent files by printing an error message
            if (e instanceof NoSuchFileException)
            {
                System.err.println("File not found: " + file);
                return FileVisitResult.CONTINUE;
            }
            throw e;
        }
    }
}
