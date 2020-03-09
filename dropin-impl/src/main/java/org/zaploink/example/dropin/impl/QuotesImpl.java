package org.zaploink.example.dropin.impl;

import org.zaploink.example.dropin.def.Quotes;

import java.util.Random;

public class QuotesImpl implements Quotes {
    private static final String version = "1.0.0";

    private static final String[] quotes = {
      "An apple a day keeps the doctor away. -- Benjamin Franklin",
      "In the middle of every difficulty lies opportunity. -- Albert Einstein",
      "Blessed is the man who expects nothing, for he shall never be disappointed. -- Alexander Pope",
      "From little acorns mighty oaks do grow. -- American proverb",
    };

    private static final Random random = new Random();

    @Override
    public String getQuoteOfTheDay() {
        return quotes[random.nextInt(quotes.length)];
    }

    @Override
    public String getVersion() {
        return version;
    }
}
