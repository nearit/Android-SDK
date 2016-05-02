package it.near.sdk.Communication;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility for building a filter to be used with our jsonApi-conformant API's
 * http://jsonapi.org/recommendations/#filtering
 *
 * Sample usage:
 * Filter filter = Filter.build().addFilter("active", "true")
 *                              .addFilter("app_id", "nevienrvjfndbkvjnfdbijsnfi");
 *
 * String stringToAppendToPath = filter.print();
 *
 * @author cattaneostefano
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
