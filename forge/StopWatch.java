package forge;

import java.util.concurrent.TimeUnit;

/**
 * A simple stopwatch to print time elapsed.
 */
public final class StopWatch
{
    private long time;

    /**
     * Creates a new StopWatch.
     *
     * The timer starts automatically.
     */
    public StopWatch()
    {
        restart();
    }

    /**
     * Restarts the timer.
     */
    public void restart()
    {
        this.time = System.nanoTime();
    }

    /**
     * Prints the time elapsed in a human-readable format.
     *
     * The displayed precision will be based upon the time elapsed.
     */
    public void printTimeElapsed()
    {
        long timeElapsed = System.nanoTime() - this.time;
        System.out.print("Time elapsed: ");

        //determine a suitable TimeUnit to use
        TimeUnit timeUnit = switch(timeElapsed)
        {
            case long n when n < 1_000 -> TimeUnit.NANOSECONDS; //less than 1 microsecond
            case long n when n < 1_000_000 -> TimeUnit.MICROSECONDS; //less than 1 millisecond
            case long n when n < 1_000_000_000 -> TimeUnit.MILLISECONDS; //less than 1 second
            default -> TimeUnit.SECONDS; //1 second or more
        };

        //print the time elapsed
        switch (timeUnit)
        {
            case NANOSECONDS, MICROSECONDS, MILLISECONDS ->
            {
                System.out.println(timeUnit.convert(timeElapsed, TimeUnit.NANOSECONDS) +
                                   " " + timeUnit.toString().toLowerCase());
            }
            case SECONDS, MINUTES, HOURS, DAYS ->
            {
                long days = TimeUnit.DAYS.convert(timeElapsed, TimeUnit.NANOSECONDS);
                long hours = TimeUnit.HOURS.convert(timeElapsed % TimeUnit.NANOSECONDS.convert(1, TimeUnit.DAYS), TimeUnit.NANOSECONDS);
                long minutes = TimeUnit.MINUTES.convert(timeElapsed % TimeUnit.NANOSECONDS.convert(1, TimeUnit.HOURS), TimeUnit.NANOSECONDS);
                long seconds = TimeUnit.SECONDS.convert(timeElapsed % TimeUnit.NANOSECONDS.convert(1, TimeUnit.MINUTES), TimeUnit.NANOSECONDS);

                if (days > 0) System.out.print(days + " days ");
                if (days > 0 || hours > 0) System.out.print(hours + " hours ");
                if (days > 0 || hours > 0 || minutes > 0) System.out.print(minutes + " minutes ");

                //print fractional seconds for closer comparisons
                long totalSeconds = TimeUnit.SECONDS.convert(timeElapsed, TimeUnit.NANOSECONDS);
                if (totalSeconds < 50)
                {
                    double fractionalSeconds = timeElapsed % TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS) / 1e9;
                    System.out.printf("%.2f seconds%n", seconds + fractionalSeconds);
                }
                else
                {
                    System.out.println(seconds + " seconds");
                }
            }
        }
    }
}
