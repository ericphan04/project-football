package com.swp.myleague.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

public class CommonFunc {

    private static final String LOCATION_DIRECTORY = "src/main/resources/static/images/Storage-Files";

    public static UUID convertStringToUUID(String str) {
        return UUID.fromString(str);
    }

    public static String uploadFile(MultipartFile file) {
        File newFile = new File(LOCATION_DIRECTORY + File.separator + file.getOriginalFilename());
        try {

            Files.copy(file.getInputStream(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return "/images/Storage-Files/" + file.getOriginalFilename();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] genQRCode(String data) {
        String result = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data="
                + URLEncoder.encode(data, StandardCharsets.UTF_8);
        try {
            URL url = new URL(result);
            InputStream in = url.openStream();
            return in.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public static <T> T parse(String str, Class<T> clazz) {
        try {
            if (!str.startsWith(clazz.getSimpleName() + "(") || !str.endsWith(")")) {
                throw new IllegalArgumentException("Invalid format for class: " + clazz.getSimpleName());
            }

            // Loại bỏ phần class name
            String content = str.substring(clazz.getSimpleName().length() + 1, str.length() - 1);
            String[] parts = content.split(", (?=[a-zA-Z0-9_]+=)");

            Map<String, String> fieldMap = new HashMap<>();
            for (String part : parts) {
                String[] keyValue = part.split("=", 2);
                fieldMap.put(keyValue[0].trim(), keyValue[1].trim());
            }

            T instance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                String valueStr = fieldMap.get(field.getName());
                if (valueStr == null)
                    continue;

                field.setAccessible(true);
                Object value = convert(valueStr, field.getType());
                field.set(instance, value);
            }

            return instance;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse object from toString: " + e.getMessage(), e);
        }
    }

    private static Object convert(String value, Class<?> type) {
        if (type == String.class)
            return value;
        if (type == int.class || type == Integer.class)
            return Integer.parseInt(value);
        if (type == long.class || type == Long.class)
            return Long.parseLong(value);
        if (type == boolean.class || type == Boolean.class)
            return Boolean.parseBoolean(value);
        if (type == double.class || type == Double.class)
            return Double.parseDouble(value);
        throw new IllegalArgumentException("Unsupported field type: " + type.getName());
    }

}
