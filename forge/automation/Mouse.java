package forge.automation;

import static forge.automation.AutomationConstants.ROBOT;

import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Timer;

import forge.ForgeUtils;


/**
 * Common methods for automating mouse events.
 */
public final class Mouse
{
    private static final int WIDTH =  Toolkit.getDefaultToolkit().getScreenSize().width;
    private static final int HEIGHT =  Toolkit.getDefaultToolkit().getScreenSize().height;

    /**
     * A private constructor to prevent non-static use of this class.
     */
    private Mouse() {}

    /**
     * Left clicks where the mouse currently is.
     */
    public static void leftClick()
    {
        ROBOT.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        ROBOT.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    /**
     * Right clicks where the mouse currently is.
     */
    public static void rightClick()
    {
        ROBOT.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        ROBOT.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    }

    /**
     * Left clicks at the given location.
     *
     * @param percentageX The percentage X location to left click
     * @param percentageY The percentage Y location to left click
     */
    public static void leftClick(double percentageX, double percentageY)
    {
        move(percentageX, percentageY);
        ROBOT.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        ROBOT.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    /**
     * Left clicks at the given location.
     *
     * @param x The X pixel to left click
     * @param y The Y pixel to left click
     */
    public static void leftClickPixel(int x, int y)
    {
        movePixel(x, y);
        leftClick();
    }

    /**
     * Left clicks at the given location.
     *
     * @param point The point, percentage based, to click on
     */
    public static void leftClick(Point2D point)
    {
        leftClick(point.getX(), point.getY());
    }

    /**
     * Right clicks at the given location.
     *
     * @param percentageX The percentage X location to right click
     * @param percentageY The percentage Y location to right click
     */
    public static void rightClick(double percentageX, double percentageY)
    {
        move(percentageX, percentageY);
        ROBOT.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        ROBOT.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    }

    /**
     * Scrolls the mouse wheel in or out the specified number of times.
     *
     * Use positive values to scroll out and negative values to scroll in.
     *
     * @param numTicks The number of ticks to scroll
     */
    public static void wheel(int numTicks)
    {
        for (int i = 0; i < Math.abs(numTicks); i++)
        {
            ROBOT.mouseWheel(numTicks > 0 ? 1 : -1);
            ROBOT.delay(10);
        }
    }

    /**
     * Scrolls the mouse wheel in the specified number of times.
     *
     * @param numTicks The number of ticks to scroll in
     */
    public static void wheelIn(int numTicks)
    {
        for (int i = 0; i < numTicks; i++)
        {
            ROBOT.mouseWheel(-1);
            ROBOT.delay(50);
        }
    }

    /**
     * Scrolls the mouse wheel in the specified number of times.
     *
     * @param numTicks The number of ticks to scroll out
     */
    public static void wheelOut(int numTicks)
    {
        for (int i = 0; i < numTicks; i++)
        {
            ROBOT.mouseWheel(1);
            ROBOT.delay(50);
        }
    }

    /**
     * Moves the mouse to the given location.
     *
     * @param percentageX The percentage X location to move to
     * @param percentageY The percentage Y location to move to
     */
    public static void move(double percentageX, double percentageY)
    {
        ROBOT.mouseMove((int)(percentageX * WIDTH), (int)(percentageY * HEIGHT));
    }

    /**
     * Moves the mouse to the given location.
     *
     * @param x The pixel X location to move to
     * @param y The pixel Y location to move to
     */
    public static void movePixel(int x, int y)
    {
        ROBOT.mouseMove(x, y);
    }

    /**
     * Drags the mouse, while clicking, from one location to the next.
     *
     * @param startPercentX The percentage X location to start dragging from
     * @param startPercentY The percentage Y location to start dragging from
     * @param endPercentX The percentage X location to stop dragging on
     * @param endPercentY The percentage Y location to stop dragging on
     */
    public static void drag(double startPercentX,
                            double startPercentY,
                            double endPercentX,
                            double endPercentY)
    {
        //move to the location and hold down left click
        move(startPercentX, startPercentY);
        ROBOT.mousePress(InputEvent.BUTTON1_DOWN_MASK);

        //calculate starting and ending positions
        int startX = (int)(startPercentX * WIDTH);
        int endX = (int)(endPercentX * WIDTH);
        int startY = (int)(startPercentY * HEIGHT);
        int endY = (int)(endPercentY * HEIGHT);

        //calculate increments
        int numSteps = Math.max(Math.abs(startX - endX), Math.abs(startY - endY));
        double xIncrement = (endX - startX)/(double)numSteps;
        double yIncrement = (endY - startY)/(double)numSteps;
        double x = startX;
        double y = startY;

        //move the mouse from the start to end location with a small delay between movements
        for (int i = 0; i < numSteps; i++, x += xIncrement, y += yIncrement)
        {
            ROBOT.delay(1);
            ROBOT.mouseMove((int)x, (int)y);
        }

        //end the drag
        ROBOT.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    /**
     * Returns the Color of the pixel at the specified location.
     *
     * @param percentageX The percentage X location to get the Color from
     * @param percentageY The percentage Y location to get the Color from
     * @return The Color of the pixel at the specified location
     */
    public static Color getColorAtLocation(double percentageX, double percentageY)
    {
        BufferedImage image = ROBOT.createScreenCapture(new Rectangle((int)(percentageX * WIDTH),
                                                                      (int)(percentageY * HEIGHT),
                                                                      1, 1));
        return new Color(image.getRGB(0, 0));
    }

    /**
     * Prints the mouse's current X and Y percentage location at regular intervals until the program ends.
     *
     * The thread started is a daemon thread and will not prevent the JVM from exiting.
     */
    public static void enablePercentDebug()
    {
        Timer timer = new Timer("Mouse Percentage Debug", true);
        timer.scheduleAtFixedRate(ForgeUtils.timerTask(() ->
        {
            Point point = MouseInfo.getPointerInfo().getLocation();
            System.out.printf("X: %.3f, Y: %.3f%n", point.getX()/WIDTH, point.getY()/HEIGHT);
        }),
        0, 50);
    }

    /**
     * Prints the mouse's current X and Y pixel location at regular intervals until the program ends.
     *
     * The thread started is a daemon thread and will not prevent the JVM from exiting.
     */
    public static void enablePixelDebug()
    {
        Timer timer = new Timer("Mouse Pixel Debug", true);
        timer.scheduleAtFixedRate(ForgeUtils.timerTask(() ->
            System.out.println(MouseInfo.getPointerInfo().getLocation())),
            0, 50);
    }
}
