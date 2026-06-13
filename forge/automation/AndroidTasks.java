package forge.automation;

import static forge.automation.AutomationConstants.ROBOT;

import java.awt.event.InputEvent;

import forge.ForgeUtils;

/**
 * A collection of utilities for use on Android mobile devices.
 *
 * These methods require an Android device to be mirrored in portrait fullscreen onto monitor 1 using scrcpy.
 * Some methods require both the phone's native aspect ratio and the mirrored aspect ratio to be 19.5:9.
 *
 * Library link:
 *      • https://github.com/Genymobile/scrcpy
 */
public final class AndroidTasks
{
    /**
     * A private constructor to prevent non-static use of this class.
     */
    private AndroidTasks() {}

    /**
     * Closes all apps by swiping upward to view all open apps and then clicking "Close All".
     */
    public static void closeAllApps()
    {
        //swipe up to view all open apps
        Mouse.move(0.500, 0.990);
        ROBOT.delay(500);
        ROBOT.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        ROBOT.delay(100);
        Mouse.move(0.500, 0.800);
        ROBOT.delay(350);
        ROBOT.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        ROBOT.delay(250);

        //click on "Close All"
        Mouse.leftClick(0.500, 0.874);
    }

    /**
     * Opens an app by swiping upwards to open the search screen, clicking "Search", typing the given app name, and selecting it.
     *
     * The phone must already be on the user's home screen.
     *
     * @param appName The name of the app to open
     */
    public static void openApp(String appName)
    {
        //swipe up to open the search screen
        Mouse.move(0.500, 0.844);
        ROBOT.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        ROBOT.delay(10);
        Mouse.move(0.500, 0.457);
        ROBOT.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

        //click the search bar at the bottom and paste the app to search for
        copyToClipboard(appName);
        Mouse.leftClick(0.500, 0.943);
        ROBOT.delay(250);
        Keyboard.paste();
        ROBOT.delay(750);

        //click on the app to open it
        Mouse.leftClick(0.411, 0.151);
    }

    /**
     * Copies the given text to the device's clipboard.
     *
     * When using scrcpy, the clipboard is shared between the PC and the device, but it appears a non-scrcpy window must first
     * be focused in order for the PC's clipboard to be copied to the device's clipboard the next time the device is focused.
     *
     * Thus, this method clicks on the second display to focus the PC (a second connected monitor is not required for this to
     * work), places the given text on the PC's clipboard, then waits for the text to be registered as copied the next time the
     * device is focused.
     *
     * @param text The text to copy to the device's clipboard
     */
    public static void copyToClipboard(String text)
    {
        Mouse.leftClick(2.020, 0.465);
        ROBOT.delay(250);
        ForgeUtils.setClipboard(text);
        ROBOT.delay(250);
    }
}
