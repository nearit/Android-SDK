package it.near.sdk.reactions.testmodels;

import java.util.Map;

public class ModelForCache {

    public String _string;
    public int _integer;
    public long _long;
    public Map<String, Object> _hashMap;

    public ModelForCache(String _string, int _integer, long _long, Map<String, Object> _hashMap) {
        this._string = _string;
        this._integer = _integer;
        this._long = _long;
        this._hashMap = _hashMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModelForCache that = (ModelForCache) o;

        if (_integer != that._integer) return false;
        if (_long != that._long) return false;
        if (!_string.equals(that._string)) return false;
        return _hashMap.equals(that._hashMap);

    }

    @Override
    public int hashCode() {
        int result = _string.hashCode();
        result = 31 * result + _integer;
        result = 31 * result + (int) (_long ^ (_long >>> 32));
        result = 31 * result + _hashMap.hashCode();
        return result;
    }
}
