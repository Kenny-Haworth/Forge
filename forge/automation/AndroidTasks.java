package forge.automation;

import static forge.automation.AutomationConstants.ROBOT;

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

    /**
     * A class with methods for using an Android keyboard.
     *
     * This class uses the Samsung Galaxy's standard keyboard layout.
     */
    public static final class Keyboard
    {
        /**
         * A private constructor to prevent non-static use of this class.
         */
        private Keyboard() {}

        /**
         * Clicks the next/done button (only available when filling in forms or fields).
         */
        public static void next()
        {
            Mouse.leftClick(0.607, 0.906);
        }

        /**
         * Clicks the close button (an upside-down caret symbol, e.g. "v").
         */
        public static void close()
        {
            Mouse.leftClick(0.591, 0.972);
        }
    }
}
