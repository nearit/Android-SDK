package it.near.sdk.trackings;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static it.near.sdk.trackings.TrackRequest.KEY_BODY;
import static it.near.sdk.trackings.TrackRequest.KEY_URL;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.is;

public class TrackRequestTest {

    private static final String DUMMY_URL = "https://any-endpoint.test.com/location/of/trackings";
    private static final String DUMMY_BODY = "{\n" +
            "    \"glossary\": {\n" +
            "        \"title\": \"example glossary\",\n" +
            "\t\t\"GlossDiv\": {\n" +
            "            \"title\": \"S\",\n" +
            "\t\t\t\"GlossList\": {\n" +
            "                \"GlossEntry\": {\n" +
            "                    \"ID\": \"SGML\",\n" +
            "\t\t\t\t\t\"SortAs\": \"SGML\",\n" +
            "\t\t\t\t\t\"GlossTerm\": \"Standard Generalized Markup Language\",\n" +
            "\t\t\t\t\t\"Acronym\": \"SGML\",\n" +
            "\t\t\t\t\t\"Abbrev\": \"ISO 8879:1986\",\n" +
            "\t\t\t\t\t\"GlossDef\": {\n" +
            "                        \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\",\n" +
            "\t\t\t\t\t\t\"GlossSeeAlso\": [\"GML\", \"XML\"]\n" +
            "                    },\n" +
            "\t\t\t\t\t\"GlossSee\": \"markup\"\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";


    @Test
    public void newTrackRequest_shouldHaveFalseSendingStatus() {
        String url = DUMMY_URL;
        String body = DUMMY_BODY;
        TrackRequest dummyRequest = new TrackRequest(url, body);
        assertThat(dummyRequest.sending, is(TrackRequest.DEFAULT_SENDING_STATUS));
    }

    @Test
    public void serializationTest() throws JSONException {
        String url = DUMMY_URL;
        String body = DUMMY_BODY;
        TrackRequest requestToSerialize = new TrackRequest(url, body);
        JSONObject json = requestToSerialize.getJsonObject();
        assertThat(json.getString(KEY_URL), is(url));
        assertThat(json.getString(KEY_BODY), is(body));
    }

    @Test
    public void deSerializationFromCorrectJson_shouldWork() throws JSONException {
        String expectedUrl = DUMMY_URL;
        String expectedBody = DUMMY_BODY;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_URL, expectedUrl);
        jsonObject.put(KEY_BODY, expectedBody);
        TrackRequest trackRequest = TrackRequest.fromJsonObject(jsonObject);
        assertThat(trackRequest.url, is(expectedUrl));
        assertThat(trackRequest.body, is(expectedBody));
    }

    @Test(expected = JSONException.class)
    public void deSerializationFromEmptyJson_shouldNotWork() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        TrackRequest.fromJsonObject(jsonObject);
    }

    @Test(expected = JSONException.class)
    public void deSerializationWithNullValues_shouldNotWork() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_URL, null);
        jsonObject.put(KEY_BODY, null);
        TrackRequest.fromJsonObject(jsonObject);
    }

    @Test
    public void sameTrackings_shouldBeEqual() {
        TrackRequest requestA = new TrackRequest(DUMMY_URL, DUMMY_BODY);
        TrackRequest requestB = new TrackRequest(DUMMY_URL, DUMMY_BODY);
        assertThat(requestA, is(requestB));
        // sending status should not be considered
        requestA.sending = false;
        requestB.sending = true;
        assertThat(requestA, is(requestB));
        // different url requests should not be equal
        requestA = new TrackRequest("a", DUMMY_BODY);
        requestB = new TrackRequest("b", DUMMY_BODY);
        assertThat(requestA, is(not(requestB)));
        // different body requests should not be equal
        requestA = new TrackRequest(DUMMY_URL, "a");
        requestB = new TrackRequest(DUMMY_URL, "b");
        assertThat(requestA, is(not(requestB)));
    }
}