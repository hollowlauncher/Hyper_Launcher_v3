package net.kdt.pojavlaunch.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.io.File;
import net.kdt.pojavlaunch.Tools;

public class ModMetadataReader {
    public static class ModMetadata {
        public String id;
        public String name;
        public String version;
        public Bitmap icon;
    }

    public static ModMetadata getMetadata(File file) {
        String fileName = file.getName();
        if (!fileName.endsWith(".jar") && !fileName.endsWith(".jar.disabled")) return null;
        try (ZipFile zip = new ZipFile(file)) {
            ModMetadata metadata = new ModMetadata();
            
            // Try fabric.mod.json
            ZipEntry fabricEntry = zip.getEntry("fabric.mod.json");
            if (fabricEntry != null) {
                try (InputStream is = zip.getInputStream(fabricEntry)) {
                    JsonObject json = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();
                    if (json.has("id")) metadata.id = json.get("id").getAsString();
                    if (json.has("name")) metadata.name = json.get("name").getAsString();
                    if (json.has("version")) metadata.version = json.get("version").getAsString();
                    if (json.has("icon")) {
                        String iconP = null;
                        if (json.get("icon").isJsonPrimitive()) {
                            iconP = json.get("icon").getAsString();
                        } else if (json.get("icon").isJsonObject()) {
                            JsonObject iconObj = json.getAsJsonObject("icon");
                            if (iconObj.size() > 0) {
                                iconP = iconObj.entrySet().iterator().next().getValue().getAsString();
                            }
                        }
                        
                        if (iconP != null) {
                            ZipEntry iconEntry = zip.getEntry(iconP);
                            if (iconEntry != null) {
                                try (InputStream iconIs = zip.getInputStream(iconEntry)) {
                                    metadata.icon = BitmapFactory.decodeStream(iconIs);
                                }
                            }
                        }
                    }
                }
            }
            
            // Try mcmod.info (Forge)
            if (metadata.name == null) {
                ZipEntry forgeEntry = zip.getEntry("mcmod.info");
                if (forgeEntry != null) {
                    try (InputStream is = zip.getInputStream(forgeEntry)) {
                        String content = Tools.read(is);
                        try {
                            JsonObject json;
                            if (content.trim().startsWith("[")) {
                                json = JsonParser.parseString(content).getAsJsonArray().get(0).getAsJsonObject();
                            } else {
                                json = JsonParser.parseString(content).getAsJsonObject();
                            }
                            if (json.has("modid")) metadata.id = json.get("modid").getAsString();
                            if (json.has("name")) metadata.name = json.get("name").getAsString();
                            if (json.has("version")) metadata.version = json.get("version").getAsString();
                        } catch (Exception ignored) {}
                    }
                }
            }

            return metadata;
        } catch (Exception e) {
            return null;
        }
    }
}
