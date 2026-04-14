package com.ootd.fitme.infrastructure.scraper;

public interface Scraper {
    ScrapedData scrape(String url);
}
