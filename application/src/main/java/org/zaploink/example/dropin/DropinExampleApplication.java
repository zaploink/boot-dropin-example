package org.zaploink.example.dropin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.zaploink.example.dropin.def.Quotes;

import java.util.logging.Logger;

/**
 * DARPnet application, including Server and Vaadin UI.
 */
@SpringBootApplication
public class DropinExampleApplication {

    private final Logger LOG = Logger.getLogger("quote-of-the-day");

    @Autowired
    void printQuoteOfTheDay(Quotes quotes) {
        LOG.info("----------------------------------------------------------------------------------------------");
        LOG.info(quotes.getQuoteOfTheDay());
        LOG.info("----------------------------------------------------------------------------------------------");
    }

    public static void main(String[] args) {
        SpringApplication.run(DropinExampleApplication.class, args);
    }

}
