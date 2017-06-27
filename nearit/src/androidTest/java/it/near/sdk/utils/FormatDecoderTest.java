package it.near.sdk.utils;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.zip.DataFormatException;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class FormatDecoderTest {

    FormatDecoder formatDecoder;

    @Before
    public void setUp() throws Exception {
        formatDecoder = new FormatDecoder();
    }

    @Test
    public void shouldDecodeString() throws IOException, DataFormatException {
        String toDecode = "eJyNzMEKgzAMANB/ydmMpja17XfstItEk8IYqIiyg/jvc+wHdn+8A2RZ+qdC\nAZG2VtUWExFhaJPhwKyomb3yqLF6Bw2M87TZtEE5YH5B2dbdzgbm92Tr74ld\nMlPfIafsMHDNmDwFrMISBuEUxL7ParKZ9nJV4B116CJ6f6dYOBbKt5SJKT0u\nui/6Hz0/JMc4wg==";
        byte[] bytes = formatDecoder.decodeBae64(toDecode);
        String decoded = formatDecoder.decompressZLIB(bytes);
        String expected = "{\"app_id\":\"aa3ffdd3-8111-438e-b55d-d952d5cd6f20\",\"content\":{\"ok\":true},\"owner_id\":\"678eed27-5890-45f9-8214-fa5a4ba584ae\",\"created_at\":\"2017-06-22T16:56:19.891518Z\",\"updated_at\":\"2017-06-22T16:56:19.891518Z\"}";
        assertEquals(expected, decoded);
    }

}