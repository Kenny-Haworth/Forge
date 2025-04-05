package forge.automation;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

/**
 * Common methods for automating mouse events.
 */
public final class Mouse
{
    private static final int WIDTH;
    private static final int HEIGHT;
    private static final Robot ROBOT;

    //initializes all static variables
    static
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        WIDTH = screenSize.width;
        HEIGHT = screenSize.height;

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
            ROBOT.delay(10);
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
            ROBOT.delay(10);
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
     * @param startPercentageX The percentage X location to start dragging from
     * @param startPercentageY The percentage Y location to start dragging from
     * @param endPercentageX The percentage X location to stop dragging on
     * @param endPercentageY The percentage Y location to stop dragging on
     */
    public static void drag(double startPercentageX,
                            double startPercentageY,
                            double endPercentageX,
                            double endPercentageY)
    {
        //move to the location and hold down left click
        move(startPercentageX, startPercentageY);
        ROBOT.mousePress(InputEvent.BUTTON1_DOWN_MASK);

        //calculate starting and ending positions
        int startX = (int)(startPercentageX * WIDTH);
        int endX = (int)(endPercentageX * WIDTH);
        int startY = (int)(startPercentageY * HEIGHT);
        int endY = (int)(endPercentageY * HEIGHT);

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
     * @return
     */
    public static Color getColorAtLocation(double percentageX, double percentageY)
    {
        BufferedImage image = ROBOT.createScreenCapture(new Rectangle((int)(percentageX * WIDTH), (int)(percentageY * HEIGHT), 1, 1));
        return new Color(image.getRGB(0, 0));
    }

    /**
     * Sleeps for the specified number of milliseconds.
     *
     * @param milliseconds The number of milliseconds to sleep
     */
    public static void delay(int milliseconds)
    {
        ROBOT.delay(milliseconds);
    }

    /**
     * Prints the mouse's current X and Y percentage location at regular intervals until the program ends.
     *
     * The thread started is a daemon thread and will not prevent the JVM from exiting.
     */
    public static void enablePercentageDebug()
    {
        Timer timer = new Timer(50, _ ->
        {
            Point point = MouseInfo.getPointerInfo().getLocation();
            System.out.printf("X: %.3f, Y: %.3f%n", point.getX()/WIDTH, point.getY()/HEIGHT);

        });
        timer.setInitialDelay(0);
        timer.start();
    }

    /**
     * Prints the mouse's current X and Y pixel location at regular intervals until the program ends.
     *
     * The thread started is a daemon thread and will not prevent the JVM from exiting.
     */
    public static void enablePixelDebug()
    {
        Timer timer = new Timer(50, _ -> System.out.println(MouseInfo.getPointerInfo().getLocation()));
        timer.setInitialDelay(0);
        timer.start();
    }
}
