package src;

import java.util.TimerTask;

/**
 * A collection of commonly used utility methods.
 */
public final class ForgeUtils
{
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
}
