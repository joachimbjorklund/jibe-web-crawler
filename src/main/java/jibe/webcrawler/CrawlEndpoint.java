package jibe.webcrawler;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import jersey.repackaged.com.google.common.base.Throwables;
import org.glassfish.jersey.server.ChunkedOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
@Component
@Path("crawl-endpoint")
public class CrawlEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlEndpoint.class);

    private final static ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);

    private BlockingQueue<PageTitle> queue = new ArrayBlockingQueue<>(10, true);
    @Inject
    private EventBus eventBus;

    @PostConstruct
    private void init() {
        LOGGER.info("init");
        eventBus.register(this);
    }

    @GET
    public Response crawl(@QueryParam("seed") String seed) {

        LOGGER.debug(this.toString());

        final ChunkedOutput<String> output = new ChunkedOutput<>(String.class);

        EXECUTOR_SERVICE.execute(() -> {
            startCrawling(seed);
        });

        EXECUTOR_SERVICE.execute(() -> {

            boolean error = false;
            try {
                PageTitle pageTitle;

                Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                int maxOut = 100000;
                while ((maxOut-- > 0) && !error && !output.isClosed() && ((((pageTitle = getNextResult()))) != null)) {
                    try {
                        output.write(gson.toJson(pageTitle, PageTitle.class) + "\n");
                    } catch (IOException e) {
                        error = true;
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                try {
                    output.close();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        });

        return Response.ok(output).build();
    }

    @Subscribe
    public void newPageTitle(PageTitle pageTitle) {
        LOGGER.debug("newPageTitle: " + pageTitle + ", q: " + queue.size());
        try {
            queue.put(pageTitle);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private void startCrawling(String seed) {
        int numberOfCrawlers = 7;

        CrawlConfiguration config = new CrawlConfiguration();
        config.setSeed(seed);
        config.setCrawlStorageFolder("./crawler");

        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController crawlController;
        try {
            crawlController = new CrawlController(config, pageFetcher, robotstxtServer);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */

        crawlController.addSeed(seed);

        crawlController.setCustomData(eventBus);
        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        crawlController.start(Crawler.class, numberOfCrawlers);
    }

    private PageTitle getNextResult() {
        try {
            return queue.take();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
