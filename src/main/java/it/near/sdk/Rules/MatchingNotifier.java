package it.near.sdk.Rules;

import it.near.sdk.Models.Matching;

/**
 * Created by cattaneostefano on 24/03/16.
 */
public interface MatchingNotifier {
    public abstract void onRuleFullfilled(Matching matching);
}
