package jibe.webcrawler;

import com.google.common.eventbus.EventBus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 *
 */
@SpringBootApplication
public class CrawlerApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(CrawlerApplication.class, args);
    }

    @Bean
    public EventBus getEventBus(){
        return new EventBus();
    }
}
