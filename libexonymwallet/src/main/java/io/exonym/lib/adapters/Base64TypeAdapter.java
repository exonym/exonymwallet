package io.exonym.lib.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;

public class Base64TypeAdapter extends TypeAdapter<byte[]> {

    @Override
    public void write(JsonWriter out, byte[] value) throws IOException {
        if (value == null) {
            out.nullValue();

        } else {
            out.value(Base64.encodeBase64String(value));

        }
    }

    @Override
    public byte[] read(JsonReader in) throws IOException {
        try {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            } else {
                String data = in.nextString();
                return Base64.decodeBase64(data);
            }
        } catch (Exception e) {
            throw new IOException("Wrapper", e);

        }
    }
}
