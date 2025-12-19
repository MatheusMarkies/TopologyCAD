package com.brasens.functions;

import com.brasens.model.io.ProjectSaveState;
import com.google.gson.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.Base64;

import javafx.embed.swing.SwingFXUtils; // Requer módulo javafx.swing

public class ProjectFileManager {

    // Configura o GSON para lidar com JavaFX Image e LocalDate
    private static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Image.class, new ImageAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .setPrettyPrinting()
                .create();
    }

    public static void saveProject(File file, ProjectSaveState state) throws IOException {
        Gson gson = createGson();
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(state, writer);
        }
    }

    public static ProjectSaveState loadProject(File file) throws IOException {
        Gson gson = createGson();
        try (Reader reader = new FileReader(file)) {
            return gson.fromJson(reader, ProjectSaveState.class);
        }
    }

    // --- ADAPTADOR PARA SALVAR IMAGEM COMO TEXTO (BASE64) ---
    private static class ImageAdapter implements JsonSerializer<Image>, JsonDeserializer<Image> {
        @Override
        public JsonElement serialize(Image src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            try {
                BufferedImage bImage = SwingFXUtils.fromFXImage(src, null);
                ByteArrayOutputStream s = new ByteArrayOutputStream();
                ImageIO.write(bImage, "png", s);
                byte[] res = s.toByteArray();
                String encoded = Base64.getEncoder().encodeToString(res);
                return new JsonPrimitive(encoded);
            } catch (Exception e) {
                return JsonNull.INSTANCE;
            }
        }

        @Override
        public Image deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                byte[] data = Base64.getDecoder().decode(json.getAsString());
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                BufferedImage bImage = ImageIO.read(bis);
                return SwingFXUtils.toFXImage(bImage, null);
            } catch (IOException e) {
                return null;
            }
        }
    }

    private static class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
        @Override
        public JsonElement serialize(LocalDate date, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            // Salva como Texto ISO (ex: "2025-12-18")
            return new JsonPrimitive(date.toString());
        }

        @Override
        public LocalDate deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            // Lê o texto e converte de volta para Objeto LocalDate
            if (json.isJsonNull() || json.getAsString().isEmpty()) return null;
            return LocalDate.parse(json.getAsString());
        }
    }
}