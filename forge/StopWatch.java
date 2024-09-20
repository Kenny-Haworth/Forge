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
    private void restart()
    {
        this.time = System.nanoTime();
    }

    /**
     * Prints the time elapsed in a human-readable format.
     *
     * @param timeUnit For TimeUnits with a precision smaller than seconds (NANOSECONDS, MICROSECONDS, MILLISECONDS),
     *                 prints the time elapsed in the given TimeUnit's precision. For larger units (SECONDS, MINUTES,
     *                 HOURS, DAYS), prints a breakdown of days, hours, minutes, and seconds.
     */
    public void printTimeElapsed(TimeUnit timeUnit)
    {
        long timeElapsed = System.nanoTime() - time;
        System.out.print("Time elapsed: ");

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
                System.out.println(seconds + " seconds");
            }
        }
    }
}
