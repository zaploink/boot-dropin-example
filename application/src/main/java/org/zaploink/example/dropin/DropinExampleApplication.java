package org.zaploink.example.dropin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.zaploink.example.dropin.def.Quotes;

import java.util.logging.Logger;

/**
 * DARPnet application, including Server and Vaadin UI.
 */
@SpringBootApplication
public class DropinExampleApplication {

    private final static String QUOTES_IMPL_CLASS = "org.zaploink.dropins.QuotesImpl";
    private final Logger LOG = Logger.getLogger("quote-of-the-day");

    @Bean
    @ConditionalOnClass(name = QUOTES_IMPL_CLASS)
    Quotes quotes() {
        try {
            return (Quotes) Class.forName(QUOTES_IMPL_CLASS).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Quotes Drop-In could not be loaded", e);
        }
    }

    @Bean
    @ConditionalOnMissingBean(Quotes.class)
    Quotes quotesFallback() {
        LOG.warning("Drop-in 'Quotes' is not present, providing fallback implementation.");
        return new Quotes() {
            @Override
            public String getQuoteOfTheDay() {
                return "The absence of a message sometimes is a presence of one. ― Hasse Jerner";
            }
        };
    }

    @Autowired
    void printQuoteOfTheDay(Quotes quotes) {
        LOG.info(quotes.getQuoteOfTheDay());
    }

    public static void main(String[] args) {
        SpringApplication.run(DropinExampleApplication.class, args);
    }

}
