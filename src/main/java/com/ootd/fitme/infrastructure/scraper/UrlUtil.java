package com.ootd.fitme.infrastructure.scraper;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlUtil {
    public static String normalize(String url) {
        if (url == null || url.isBlank()) return "";
        try {
            URI uri = new URI(url);
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null).toString();
        } catch (URISyntaxException e) {
            return url.split("\\?")[0];
        }
    }
}
