package it.near.sdk.reactions.contentplugin.model;

public class ContentLink {

    public static final String CTA_LABEL_KEY = "label";
    public static final String CTA_URL_KEY = "url";

    public String label;
    public String url;

    public ContentLink(String label, String url) {
        this.label = label;
        this.url = url;
    }
}
