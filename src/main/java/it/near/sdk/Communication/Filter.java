package it.near.sdk.Communication;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cattaneostefano on 17/03/16.
 */
public class Filter {

    String filter ="";
    private HashMap<String, String> valueMap;

    public Filter() {
        valueMap = new HashMap<>();
    }

    public Filter addFilter(String name, String value){
        if (name != null && value != null) {
            valueMap.put(name, value);
        }
        return this;
    }

    public String print(){
        if ( valueMap.size() != 0 ){
            filter = "?";
            for(Map.Entry<String, String> entry : valueMap.entrySet()){
                filter = filter.concat("filter[" + entry.getKey() + "]=" + entry.getValue() + "&");
            }
            filter = filter.substring(0, filter.length() - 1);
        }
        return filter;
    }


    public static Filter build(){
        return new Filter();
    }


}
