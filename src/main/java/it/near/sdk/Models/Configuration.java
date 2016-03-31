package it.near.sdk.Models;

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
    List<NearBeacon> beaconList;
    List<Content> contentList;

    public List<Matching> getMatchingList() {
        return matchingList;
    }

    public void setMatchingList(List<Matching> matchingList) {
        this.matchingList = matchingList;
    }

    public List<NearBeacon> getBeaconList() {
        return beaconList;
    }

    public List<Content> getContentList() {
        return contentList;
    }

    public void setContentList(List<Content> contentList) {
        this.contentList = contentList;
    }

    public void setBeaconList(List<NearBeacon> beaconList) {
        this.beaconList = beaconList;
    }

    public void addBeacon(NearBeacon beacon) {
        if (beaconList == null) {
            beaconList = new ArrayList<NearBeacon>();
        }
        beaconList.add(beacon);
    }

    public boolean hasBeacon(org.altbeacon.beacon.Beacon beacon) {
        if ( beaconList == null || beaconList.size()==0 ){
            return false;
        }
        for (NearBeacon appBeacon : beaconList){
            if (appBeacon.isLike(beacon))
                return true;
        }
        return false;
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



    // Those next three methods work on the assumption that there's only one matching for a beacon

    public Content getContentFromBeacon(NearBeacon beacon) {
        Matching matching = getMatchingFromBeacon(beacon);
        return getContentFromId(matching.getContent_id());
    }

    public Matching getMatchingFromBeacon(NearBeacon beacon) {
        for ( Matching m : matchingList ) {
            if (m.getBeacon_id().equals(beacon.getId())){
                return m;
            }
        }
        return null;
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
