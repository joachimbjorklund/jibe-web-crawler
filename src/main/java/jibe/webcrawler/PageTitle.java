package jibe.webcrawler;

import com.google.common.base.Objects;

/**
 *
 */
public class PageTitle {

    private final String url;
    private final String title;

    public PageTitle(String url, String title){
        this.url = url;
        this.title = title;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("title", title)
                .add("url", url)
                .toString();
    }
}
