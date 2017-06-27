package it.near.sdk.utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class FormatDecoder {

    public byte[] decodeBae64(String encoded) throws IllegalArgumentException {
        return Base64.decode(encoded, Base64.DEFAULT);
    }

    public String decompressZLIB(byte[] compressed) throws DataFormatException, UnsupportedEncodingException {
        // Decompress the bytes
        Inflater decompresser = new Inflater();
        decompresser.setInput(compressed, 0, compressed.length);
        byte[] result = new byte[2048];
        int resultLength = decompresser.inflate(result);
        decompresser.end();

        // Decode the bytes into a String
        return new String(result, 0, resultLength, "UTF-8");
    }
}
