package forge;

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
        System.out.println("Time elapsed: " + ForgeUtils.timeToStr(System.nanoTime() - this.time));
    }
}
