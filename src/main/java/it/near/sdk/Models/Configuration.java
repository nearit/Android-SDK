package it.near.sdk.Models;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.List;

import it.near.sdk.Utils.ULog;

/**
 * Object representing the configuration of an "app". As of release 1, it consist of beacons, contents and matchings.
 * Created by cattaneostefano on 17/03/16.
 */
public class Configuration {

    private static final String TAG = "Configuration";
    List<Matching> matchingList;
    List<Content> contentList;

    public List<Matching> getMatchingList() {
        return matchingList;
    }

    public void setMatchingList(List<Matching> matchingList) {
        this.matchingList = matchingList;
    }


    public List<Content> getContentList() {
        return contentList;
    }

    public void setContentList(List<Content> contentList) {
        this.contentList = contentList;
    }


    public void addContent(Content content) {

        if (contentList == null || contentList.isEmpty()) {
            contentList = new ArrayList<Content>();
        }

        contentList.add(content);

    }

    public boolean hasContent(Content content) {

        if(contentList.isEmpty()) {
            return false;
        }

        for (Content toCheck : contentList) {
            if (toCheck.getId().equals(content.getId())) {
                return true;
            }
        }

        return false;
    }


    public Content getContentFromId(String content_id) {
        for (Content c : contentList){
            if (c.getId().equals(content_id)){
                return c;
            }
        }
        return null;
    }

}
