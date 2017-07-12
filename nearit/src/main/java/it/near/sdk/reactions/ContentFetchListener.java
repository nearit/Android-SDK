package it.near.sdk.reactions;

public interface ContentFetchListener<ReactionBundle> {
    void onContentFetched(ReactionBundle content, boolean cached);

    void onContentFetchError(String error);
}
