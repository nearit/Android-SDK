package it.near.sdk;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import it.near.sdk.logging.NearLogger;

public class TestUtils {

    public static JSONObject readJsonFile(Class clazz, String fileName) throws Exception {
        InputStream inputStream = clazz.getClassLoader().getResourceAsStream(fileName);
        String toParse = TestUtils.readTextStream(inputStream);
        return new JSONObject(toParse);
    }

    public static String readTextStream(InputStream inputStream) throws Exception {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }

    public static NearLogger emptyLogger() {
        return new NearLogger() {
            @Override
            public void v(String tag, String msg) {

            }

            @Override
            public void v(String tag, String msg, Throwable tr) {

            }

            @Override
            public void d(String tag, String msg) {

            }

            @Override
            public void d(String tag, String msg, Throwable tr) {

            }

            @Override
            public void i(String tag, String msg) {

            }

            @Override
            public void i(String tag, String msg, Throwable tr) {

            }

            @Override
            public void w(String tag, String msg) {

            }

            @Override
            public void w(String tag, String msg, Throwable tr) {

            }

            @Override
            public void e(String tag, String msg) {

            }

            @Override
            public void e(String tag, String msg, Throwable tr) {

            }
        };
    }
}
