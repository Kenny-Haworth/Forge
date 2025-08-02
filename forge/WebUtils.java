package forge;

import java.io.IOException;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * A collection of web-based utility methods.
 */
public final class WebUtils
{
    //a list of modern, commonly used User-Agent Strings based on traffic patterns in mid 2025
    private static final String[] USER_AGENTS =
    {
        //Windows - Chrome
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.6422.78 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.6367.93 Safari/537.36",
        "Mozilla/5.0 (Windows NT 11.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6441.61 Safari/537.36",

        //Windows - Firefox
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:126.0) Gecko/20100101 Firefox/126.0",
        "Mozilla/5.0 (Windows NT 11.0; Win64; x64; rv:127.0) Gecko/20100101 Firefox/127.0",

        //macOS - Safari
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Safari/605.1.15",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15",

        //macOS - Chrome
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.6422.78 Safari/537.36",

        //Linux - Chrome
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.6367.93 Safari/537.36",
        "Mozilla/5.0 (X11; Linux x86_64) Gecko/20100101 Firefox/126.0",

        //Android - Chrome
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.6441.61 Mobile Safari/537.36",
        "Mozilla/5.0 (Linux; Android 13; Samsung Galaxy S24 Ultra) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.6422.78 Mobile Safari/537.36",

        //iOS - Safari
        "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1",
        "Mozilla/5.0 (iPad; CPU OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1"
    };

    private static final Random RANDOM = new Random();
    private static long timeOfLastFetch; //time of last fetched URL

    /**
     * A private constructor to prevent non-static use of this class.
     */
    private WebUtils() {}

    /**
     * Retrieves the HTML document from the given URL.
     *
     * @param url The URL to fetch from
     * @return An HTML Document
     * @throws IOException If an error occurs fetching the document
     */
    public static Document fetchUrlAsHtml(String url) throws IOException
    {
        return Jsoup.connect(url)
                    .userAgent(getRandomUserAgent())
                    .referrer("https://www.google.com/")
                    .timeout(10000)
                    .get();
    }

    /**
     * Retrieves the HTML document from the given URL.
     *
     * This method waits 3-5 seconds between fetching URLs to avoid an IP ban when repeatedly accessing the same resource.
     * The delay is program-wide and irrespective of the thread running.
     *
     * @param url The URL to fetch from
     * @return An HTML Document
     * @throws Exception If an error occurs fetching the document
     */
    public static synchronized Document fetchUrlAsHtmlWithDelay(String url) throws Exception
    {
        //wait 3-5 seconds between requests to avoid being IP banned
        long delayMs = 1000 * (3 + RANDOM.nextInt(3));
        Thread.sleep(Math.max(0, delayMs - (long)((System.nanoTime() - timeOfLastFetch)/1e6)));
        timeOfLastFetch = System.nanoTime();

        //headless connections run more risk of bans, so spoof the userAgent and referrer
        return Jsoup.connect(url)
                    .userAgent(getRandomUserAgent())
                    .referrer("https://www.google.com/")
                    .timeout(10000)
                    .get();
    }

    /**
     * Returns a randomized user agent.
     *
     * @return A randomized user agent
     */
    private static String getRandomUserAgent()
    {
        return USER_AGENTS[RANDOM.nextInt(USER_AGENTS.length)];
    }
}
