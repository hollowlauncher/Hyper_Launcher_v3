package net.kdt.pojavlaunch.utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Minimal NBT reader for level.dat
 */
public class NBT {
    public static final int TAG_END = 0;
    public static final int TAG_BYTE = 1;
    public static final int TAG_SHORT = 2;
    public static final int TAG_INT = 3;
    public static final int TAG_LONG = 4;
    public static final int TAG_FLOAT = 5;
    public static final int TAG_DOUBLE = 6;
    public static final int TAG_BYTE_ARRAY = 7;
    public static final int TAG_STRING = 8;
    public static final int TAG_LIST = 9;
    public static final int TAG_COMPOUND = 10;
    public static final int TAG_INT_ARRAY = 11;
    public static final int TAG_LONG_ARRAY = 12;

    public static Tag read(InputStream is) throws IOException {
        try (DataInputStream dis = new DataInputStream(new GZIPInputStream(is))) {
            int type = dis.readByte();
            if (type == TAG_END) return null;
            String name = dis.readUTF();
            return readTag(dis, type, name);
        }
    }

    private static Tag readTag(DataInputStream dis, int type, String name) throws IOException {
        switch (type) {
            case TAG_BYTE: return new Tag(name, dis.readByte(), type);
            case TAG_SHORT: return new Tag(name, dis.readShort(), type);
            case TAG_INT: return new Tag(name, dis.readInt(), type);
            case TAG_LONG: return new Tag(name, dis.readLong(), type);
            case TAG_FLOAT: return new Tag(name, dis.readFloat(), type);
            case TAG_DOUBLE: return new Tag(name, dis.readDouble(), type);
            case TAG_BYTE_ARRAY:
                int bLen = dis.readInt();
                byte[] bData = new byte[bLen];
                dis.readFully(bData);
                return new Tag(name, bData, type);
            case TAG_STRING: return new Tag(name, dis.readUTF(), type);
            case TAG_LIST:
                int lType = dis.readByte();
                int lLen = dis.readInt();
                List<Tag> lData = new ArrayList<>(lLen);
                for (int i = 0; i < lLen; i++) lData.add(readTag(dis, lType, ""));
                return new Tag(name, lData, type);
            case TAG_COMPOUND:
                Map<String, Tag> cData = new HashMap<>();
                int cType;
                while ((cType = dis.readByte()) != TAG_END) {
                    String cName = dis.readUTF();
                    cData.put(cName, readTag(dis, cType, cName));
                }
                return new Tag(name, cData, type);
            case TAG_INT_ARRAY:
                int iLen = dis.readInt();
                int[] iData = new int[iLen];
                for (int i = 0; i < iLen; i++) iData[i] = dis.readInt();
                return new Tag(name, iData, type);
            case TAG_LONG_ARRAY:
                int loLen = dis.readInt();
                long[] loData = new long[loLen];
                for (int i = 0; i < loLen; i++) loData[i] = dis.readLong();
                return new Tag(name, loData, type);
            default: throw new IOException("Unknown NBT tag type: " + type);
        }
    }

    public static class Tag {
        public final String name;
        public final Object value;
        public final int type;

        public Tag(String name, Object value, int type) {
            this.name = name;
            this.value = value;
            this.type = type;
        }

        @SuppressWarnings("unchecked")
        public Tag get(String key) {
            if (type == TAG_COMPOUND) return ((Map<String, Tag>) value).get(key);
            return null;
        }

        @SuppressWarnings("unchecked")
        public List<Tag> asList() { return (List<Tag>) value; }
        public String asString() { return (String) value; }
        public int asInt() { return (Integer) value; }
        public long asLong() { return (Long) value; }
    }
}
