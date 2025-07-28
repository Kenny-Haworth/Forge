package forge.automation;

import java.awt.AWTException;
import java.awt.Robot;

/**
 * Supplies constants used by other automation classes.
 */
public final class AutomationConstants
{
    /**
     * A private constructor to prevent non-static use of this class.
     */
    private AutomationConstants() {}

    //initializes ROBOT
    public static final Robot ROBOT; static
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
}
