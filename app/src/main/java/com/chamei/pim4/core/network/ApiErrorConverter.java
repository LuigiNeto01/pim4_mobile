package com.chamei.pim4.core.network;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Converte corpo de erro em texto legível quando a API retorna JSON com { message }.
 */
public class ApiErrorConverter {
    private final Converter<ResponseBody, Object> converter;

    public ApiErrorConverter(Converter<ResponseBody, Object> converter) {
        this.converter = converter;
    }

    public String toMessage(ResponseBody body, String fallback) {
        if (body == null) return fallback;
        try {
            Object obj = converter.convert(body);
            if (obj instanceof java.util.Map) {
                Object msg = ((java.util.Map<?, ?>) obj).get("message");
                if (msg != null) return msg.toString();
            }
            return obj != null ? obj.toString() : fallback;
        } catch (IOException e) {
            return fallback;
        }
    }
}
