package org.zaploink.example.dropin.impl;

import org.zaploink.example.dropin.def.Quotes;

import java.util.concurrent.ThreadLocalRandom;

public class QuotesImpl implements Quotes {
    private String[] quotes = {
      "An apple a day keeps the doctor away. -- Benjamin Franklin",
      "In the middle of every difficulty lies opportunity. -- Albert Einstein",
      "Blessed is the man who expects nothing, for he shall never be disappointed. -- Alexander Pope",
      "From little acorns mighty oaks do grow. -- American proverb",
    };

    @Override
    public String getQuoteOfTheDay() {
        return quotes[ThreadLocalRandom.current().nextInt(quotes.length)];
    }
}
