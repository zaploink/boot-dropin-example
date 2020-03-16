package org.zaploink.example.dropin;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.zaploink.example.dropin.def.Quotes;

import java.util.logging.Logger;

@Component
public class QuotesProvider {
    private final static Logger LOG = Logger.getLogger(QuotesProvider.class.getName());
    private final static String QUOTES_IMPL_CLASS = "org.zaploink.dropins.QuotesImpl";

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
                return "The absence of a message sometimes is a presence of one. -- Hasse Jerner";
            }
        };
    }

}
