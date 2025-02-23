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

    /**
     * Prints the number of elements processed, percent complete, and estimated time remaining.
     *
     * @param elementsProcessed The number of elements processed thus far
     * @param totalElements The total number of elements to process
     * @param unit The unit of measurement for the elements (e.g. "MB"), or an empty String to not specify a unit
     * @return True if the processing is complete, false otherwise
     */
    public boolean printEstimatedTimeRemaining(long elementsProcessed, long totalElements, String unit)
    {
        //if there are no elements to process, there is no time remaining
        if (totalElements <= 0)
        {
            //note that "\033[K" deletes everything from the cursor to the end of line
            System.out.print("\r\033[KProgress: 0/0 " + unit + ", 100.00%, Time remaining: Complete");
            return true;
        }

        long timeElapsed = System.nanoTime() - this.time;
        double percentComplete = (double)elementsProcessed / totalElements;

        String strPercentComplete = String.format("%.2f%%", percentComplete * 100);
        long nanoSecondsRemaining = (long)((timeElapsed / percentComplete) - timeElapsed);
        String timeRemaining = ForgeUtils.timeToStr(nanoSecondsRemaining);

        System.out.print("\r\033[KProgress: " + elementsProcessed + "/" + totalElements + " " + unit +
                         ", " + strPercentComplete + ", Time remaining: " + (timeRemaining.isEmpty() ? "Complete" : timeRemaining));
        System.out.flush();

        return percentComplete >= 1;
    }
}
