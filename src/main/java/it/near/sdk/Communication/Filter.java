package it.near.sdk.Communication;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility for building a filter to be used with our jsonApi-conformant API's
 *
 * Sample usage:
 * <pre>
 * {@code
 * Filter filter = Filter.build().addFilter("active", "true")
 *                  .addFilter("app_id", "nevienrvjfndbkvjnfdbijsnfi");
 *
 * String stringToAppendToPath = filter.print();  // output = ?filter[active]=true&filter[app_id]=nevienrvjfndbkvjnfdbijsnfi
 * }
 * </pre>
 *
 * @author cattaneostefano
 * @see <a href="http://jsonapi.org/format/1.0/">jsonAPI 1.0 specifications</a>
 */
public class Filter {

    String filter ="";
    private HashMap<String, String> valueMap;

    public Filter() {
        valueMap = new HashMap<>();
    }

    /**
     * Add a filter name-value String pair
     * @param name filter name
     * @param value filter value/s
     * @return
     */
    public Filter addFilter(String name, String value){
        if (name != null && value != null) {
            valueMap.put(name, value);
        }
        return this;
    }

    /**
     * Print a jsonAPI filter query section with a leading '?'
     * @return query parameters section
     */
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

    /**
     * Return an empty filter instance
     * @return filter instance
     */
    public static Filter build(){
        return new Filter();
    }


}
