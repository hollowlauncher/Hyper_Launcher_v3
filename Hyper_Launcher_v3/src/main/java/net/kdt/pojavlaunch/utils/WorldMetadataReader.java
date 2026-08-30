package net.kdt.pojavlaunch.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class WorldMetadataReader {
    public static class WorldMetadata {
        public String worldName;
        public String gameMode;
        public Bitmap icon;
    }

    public static WorldMetadata getMetadata(File worldDir) {
        if (!worldDir.isDirectory()) return null;
        File levelDat = new File(worldDir, "level.dat");
        if (!levelDat.exists()) return null;

        WorldMetadata metadata = new WorldMetadata();
        try (InputStream is = new FileInputStream(levelDat)) {
            NBT.Tag root = NBT.read(is);
            if (root != null) {
                NBT.Tag data = root.get("Data");
                if (data != null) {
                    NBT.Tag levelName = data.get("LevelName");
                    if (levelName != null) metadata.worldName = levelName.asString();
                    
                    NBT.Tag gameType = data.get("GameType");
                    if (gameType != null) {
                        int type = gameType.asInt();
                        metadata.gameMode = getGameModeName(type);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        File iconFile = new File(worldDir, "icon.png");
        if (iconFile.exists()) {
            metadata.icon = BitmapFactory.decodeFile(iconFile.getAbsolutePath());
        }

        return metadata;
    }

    private static String getGameModeName(int type) {
        switch (type) {
            case 0: return "Survival";
            case 1: return "Creative";
            case 2: return "Adventure";
            case 3: return "Spectator";
            default: return "Unknown";
        }
    }
}
