package jibe.webcrawler;

import com.google.common.eventbus.EventBus;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 *
 */
public class Crawler extends WebCrawler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Crawler.class);

    private final static Pattern EXCLUDES = Pattern.compile(".*(\\.(css|js|gif|jpg|png|mp3|mp3|zip|gz))$");

    /**
     * This method receives two parameters. The first parameter is the page
     * in which we have discovered this new url and the second parameter is
     * the new url. You should implement this function to specify whether
     * the given url should be crawled or not (based on your crawling logic).
     * In this example, we are instructing the crawler to ignore urls that
     * have css, js, git, ... extensions and to only accept urls that start
     * with "http://www.ics.uci.edu/". In this case, we didn't need the
     * referringPage parameter to make the decision.
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        if (EXCLUDES.matcher(href).matches()) {
            LOGGER.debug("not accepting: " + href);
            return false;
        }

        return true;
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        LOGGER.debug("visit: " + url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();

            ((EventBus) getMyController().getCustomData()).post(
                    new PageTitle(url, htmlParseData.getTitle()));
        }
    }

    private String getSeed() {
        return ((CrawlConfiguration) getMyController().getConfig()).getSeed();
    }
}
