package it.near.sdk.reactions.testmodels;

import android.support.annotation.Nullable;

import java.util.Map;

public class ModelForCache {

    @Nullable
    public String _string;
    public int _integer;
    public long _long;
    @Nullable
    public Map<String, Object> _hashMap;

    public ModelForCache(@Nullable String _string, int _integer, long _long, Map<String, Object> _hashMap) {
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
        if (_string != null ? !_string.equals(that._string) : that._string != null) return false;
        return _hashMap != null ? _hashMap.equals(that._hashMap) : that._hashMap == null;
    }

    @Override
    public int hashCode() {
        int result = _string != null ? _string.hashCode() : 0;
        result = 31 * result + _integer;
        result = 31 * result + (int) (_long ^ (_long >>> 32));
        result = 31 * result + (_hashMap != null ? _hashMap.hashCode() : 0);
        return result;
    }
}
