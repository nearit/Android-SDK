package it.near.sdk.Communication;

import at.rags.morpheus.Deserializer;
import at.rags.morpheus.JSONAPIObject;
import at.rags.morpheus.Morpheus;
import it.near.sdk.Models.Beacon;
import it.near.sdk.R;
import it.near.sdk.Utils.ULog;

/**
 * Created by cattaneostefano on 15/03/16.
 */
public class NearItServer {

    private static final String TAG = "NearItServer";

    private final String inputString = "{\n" +
            "  \"data\": {\n" +
            "    \"id\": \"076bbace-b028-4214-9e51-92bf5d51c735\",\n" +
            "    \"type\": \"beacons\",\n" +
            "    \"attributes\": {\n" +
            "      \"name\": \"becco tonante\",\n" +
            "      \"major\": 444,\n" +
            "      \"proximity_uuid\": \"fb83be70-7d75-4bcb-8362-3ed8f8e99572\",\n" +
            "      \"range\": 0,\n" +
            "      \"minor\": 45,\n" +
            "      \"color\": \"white\",\n" +
            "      \"app_id\": \"fb83be70-7d75-4bcb-8362-3ed8f8e99572\",\n" +
            "      \"creator_id\": \"3391d4b6-4e54-4217-97d1-729e78f8c134\",\n" +
            "      \"trashed\": false,\n" +
            "      \"map_placed\": false,\n" +
            "      \"map_placement_x\": null,\n" +
            "      \"map_placement_y\": null\n" +
            "    },\n" +
            "    \"relationships\": {\n" +
            "      \"matching\": {\n" +
            "        \"data\": null\n" +
            "      }\n" +
            "    },\n" +
            "    \"meta\": {\n" +
            "      \"matched\": false\n" +
            "    }\n" +
            "  }\n" +
            "}";

    public NearItServer() {
        Morpheus morpheus = new Morpheus();
        //register your resources
        Deserializer.registerResourceClass("beacons", Beacon.class);
        JSONAPIObject jsonapiObject = null;
        try {
            jsonapiObject = morpheus.parse(inputString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Beacon beacon = (Beacon)jsonapiObject.getResource();
        ULog.d(TAG, "Beacon Id: " + beacon.getId());
    }
}
