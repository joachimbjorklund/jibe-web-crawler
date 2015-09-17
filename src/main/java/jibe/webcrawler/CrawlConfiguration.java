package jibe.webcrawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;

/**
 *
 */
public class CrawlConfiguration extends CrawlConfig {
    private String seed;

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }
}
