package forge.automation;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

/**
 * Common methods for automating keyboard events.
 */
public final class Keyboard
{
    //initializes ROBOT
    private static final Robot ROBOT; static
    {
        Robot robot = null;
        try
        {
            robot = new Robot();
        }
        catch (AWTException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        ROBOT = robot;
    }

    /**
     * A private constructor to prevent non-static use of this class.
     */
    private Keyboard() {}

    /**
     * Presses Control + V to paste.
     */
    public static void paste()
    {
        ROBOT.keyPress(KeyEvent.VK_CONTROL);
        ROBOT.keyPress(KeyEvent.VK_V);
        ROBOT.keyRelease(KeyEvent.VK_V);
        ROBOT.keyRelease(KeyEvent.VK_CONTROL);
    }

    /**
     * Presses Control + A to highlight all.
     */
    public static void highlightAll()
    {
        ROBOT.keyPress(KeyEvent.VK_CONTROL);
        ROBOT.keyPress(KeyEvent.VK_A);
        ROBOT.keyRelease(KeyEvent.VK_A);
        ROBOT.keyRelease(KeyEvent.VK_CONTROL);
    }
}
