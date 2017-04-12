package it.near.sdk.morpheusnear;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Extend this resource to your custom Object you want to map.
 * You can set custom json object names and relationships via the provided annotations.
 * <pre>
 * {@code
 * public class Article extends Resource { ... }
 * }</pre>
 *
 */
public class Resource {
  private String Id;
  private Links links;
  private HashMap<String, Object> meta;
  private JSONObject jsonSourceObject;

  public Resource() {
  }

  public HashMap<String, Object> getMeta() {
    return meta;
  }

  public void setMeta(HashMap<String, Object> meta) {
    this.meta = meta;
  }

  public Links getLinks() {
    return links;
  }

  public void setLinks(Links links) {
    this.links = links;
  }

  public String getId() {
    return Id;
  }

  public void setId(String id) {
    Id = id;
  }

  public JSONObject getJsonSourceObject() {
    return jsonSourceObject;
  }

  public void setJsonSourceObject(JSONObject jsonSourceObject) {
    this.jsonSourceObject = jsonSourceObject;
  }
}

