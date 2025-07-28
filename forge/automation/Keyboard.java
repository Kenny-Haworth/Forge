package forge.automation;

import java.awt.event.KeyEvent;

import static forge.automation.AutomationConstants.ROBOT;

/**
 * Common methods for automating keyboard events.
 */
public final class Keyboard
{
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
