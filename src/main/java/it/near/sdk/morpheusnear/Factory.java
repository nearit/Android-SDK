package it.near.sdk.morpheusnear;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory to create and map {@link Resource}.
 */
public class Factory {

  private Mapper mapper = new Mapper();
  private Deserializer deserializer = new Deserializer();

  public Factory() {
  }

  /**
   * Deserializes a json object of data to the registered class.
   *
   * @param dataObject JSONObject from data
   * @param included {@literal List<Resource>} from includes to automatic match them.
   * @return Deserialized Object.
   * @throws Exception when deserializer is not able to create instance.
   */
  public Resource newObjectFromJSONObject(JSONObject dataObject, List<Resource> included) throws Exception {
    Resource realObject = null;

    try {
      realObject = deserializer.createObjectFromString(getTypeFromJson(dataObject));
    } catch (Exception e) {
      throw e;
    }

    realObject.setJsonSourceObject(dataObject);

    try {
      realObject = mapper.mapId(realObject, dataObject);
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      realObject = mapper.mapAttributes(realObject, dataObject.getJSONObject("attributes"));
    } catch (Exception e) {
      // e.printStackTrace();
      Logger.debug("JSON data does not contain attributes");
    }

    /*try {
      realObject = mapper.mapRelations(realObject, dataObject.getJSONObject("relationships"), included);
    } catch (Exception e) {
      Logger.debug("JSON data does not contain relationships");
    }*/

    try {
      assert realObject != null;
      realObject.setMeta(mapper.getAttributeMapper().createMapFromJSONObject(dataObject.getJSONObject("meta")));
    } catch (Exception e) {
      Logger.debug("JSON data does not contain meta");
    }

    try {
      realObject.setLinks(mapper.mapLinks(dataObject.getJSONObject("links")));
    } catch (JSONException e) {
      Logger.debug("JSON data does not contain links");
    }

    return realObject;
  }

  /**
   * Loops through data objects and deserializes them.
   *
   * @param dataArray JSONArray of the data node.
   * @param included {@literal List<Resource>} from includes to automatic match them.
   * @return List of deserialized objects.
   * @throws Exception when deserializer is not able to create instance.
   */
  public List<Resource> newObjectFromJSONArray(JSONArray dataArray, List<Resource> included) throws Exception {
    ArrayList<Resource> objects = new ArrayList<>();

    for (int i = 0; i < dataArray.length(); i++) {
      JSONObject jsonObject = null;

      try {
        jsonObject = dataArray.getJSONObject(i);
      } catch (JSONException e) {
        Logger.debug("Was not able to get dataArray["+i+"] as JSONObject.");
      }
      try {
        objects.add(newObjectFromJSONObject(jsonObject, included));
      } catch (Exception e) {
        throw e;
      }
    }

    // TODO possibly remove this next section
    /*for (int i = 0; i < dataArray.length(); i++) {
      JSONObject jsonObject = null;

      try {
        jsonObject = dataArray.getJSONObject(i);
      } catch (JSONException e) {
        Logger.debug("Was not able to get dataArray["+i+"] as JSONObject.");
      }
      try {
        List<Resource> res = new ArrayList<>();
        if (included!=null) res.addAll(included);
        res.addAll(objects);
        mapper.mapRelations(objects.get(i), jsonObject.getJSONObject("relationships"), res);
      } catch (Exception e) {
        Logger.debug("JSON data does not contain relationships");
      }
    }*/


    return objects;
  }

  // helper

  /**
   * Get the type of the data message.
   *
   * @param object JSONObject.
   * @return Name of the json type.
   */
  public  String getTypeFromJson(JSONObject object) {
    String type = null;
    try {
      type = object.getString("type");
    } catch (JSONException e) {
      Logger.debug("JSON data does not contain type");
    }
    return type;
  }

  public void setDeserializer(Deserializer deserializer) {
    this.deserializer = deserializer;
  }

  public Deserializer getDeserializer() {
    return deserializer;
  }


  public void setMapper(Mapper mapper) {
    this.mapper = mapper;
    this.mapper.setFactory(this);
  }

}
