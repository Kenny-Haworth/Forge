package forge.automation;

/**
 * A class with methods for using an Android keyboard.
 *
 * This class uses the Samsung Galaxy's standard keyboard layout.
 */
public final class AndroidKeyboard
{
    /**
     * A private constructor to prevent non-static use of this class.
     */
    private AndroidKeyboard() {}

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
