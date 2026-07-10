package com.visomod.export;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExportData {
    public Metadata metadata;
    public Map<String, PaletteEntry> palette;
    public List<BlockEntry> blocks;

    public ExportData(String version, int[] min, int[] max, int[] dimensions, String name) {
        this.metadata = new Metadata(version, min, max, dimensions, name);
        this.palette = new LinkedHashMap<>();
        this.blocks = new ArrayList<>();
    }

    public String toJson(boolean pretty) {
        StringBuilder sb = new StringBuilder();
        String indent1 = pretty ? "  " : "";
        String indent2 = pretty ? "    " : "";
        String indent3 = pretty ? "      " : "";
        String nl = pretty ? "\n" : "";
        String sp = pretty ? " " : "";

        sb.append("{").append(nl);
        // Metadata
        sb.append(indent1).append("\"metadata\":").append(sp).append("{").append(nl);
        sb.append(indent2).append("\"version\":").append(sp).append("\"").append(metadata.version).append("\",").append(nl);
        sb.append(indent2).append("\"name\":").append(sp).append("\"").append(metadata.name).append("\",").append(nl);
        sb.append(indent2).append("\"bounds\":").append(sp).append("{").append(nl);
        sb.append(indent3).append("\"min\":").append(sp).append("[").append(metadata.bounds.min[0]).append(",").append(sp).append(metadata.bounds.min[1]).append(",").append(sp).append(metadata.bounds.min[2]).append("],").append(nl);
        sb.append(indent3).append("\"max\":").append(sp).append("[").append(metadata.bounds.max[0]).append(",").append(sp).append(metadata.bounds.max[1]).append(",").append(sp).append(metadata.bounds.max[2]).append("]").append(nl);
        sb.append(indent2).append("},").append(nl);
        sb.append(indent2).append("\"dimensions\":").append(sp).append("[").append(metadata.dimensions.x).append(",").append(sp).append(metadata.dimensions.y).append(",").append(sp).append(metadata.dimensions.z).append("]").append(nl);
        sb.append(indent1).append("},").append(nl);

        // Palette
        sb.append(indent1).append("\"palette\":").append(sp).append("{").append(nl);
        int pIndex = 0;
        for (Map.Entry<String, PaletteEntry> entry : palette.entrySet()) {
            sb.append(indent2).append("\"").append(entry.getKey()).append("\":").append(sp).append("{");
            sb.append("\"id\":").append(sp).append(entry.getValue().id).append(",");
            sb.append("\"name\":").append(sp).append("\"").append(entry.getValue().name).append("\",");
            
            sb.append("\"properties\":").append(sp).append("{");
            int pCount = 0;
            for (Map.Entry<String, String> prop : entry.getValue().properties.entrySet()) {
                sb.append("\"").append(prop.getKey()).append("\":").append(sp).append("\"").append(prop.getValue()).append("\"");
                if (pCount++ < entry.getValue().properties.size() - 1) sb.append(",");
            }
            sb.append("}");

            if (entry.getValue().base64Texture != null) {
                sb.append(",\"base64Texture\":").append(sp).append("\"").append(entry.getValue().base64Texture).append("\"");
            }
            
            sb.append("}");
            if (pIndex++ < palette.size() - 1) sb.append(",");
            sb.append(nl);
        }
        sb.append(indent1).append("},").append(nl);

        // Blocks
        sb.append(indent1).append("\"blocks\":").append(sp).append("[").append(nl);
        for (int i = 0; i < blocks.size(); i++) {
            BlockEntry b = blocks.get(i);
            sb.append(indent2).append("{\"pos\":").append(sp).append("{\"x\":").append(b.x).append(",\"y\":").append(b.y).append(",\"z\":").append(b.z).append("},");
            sb.append("\"p\":").append(b.p).append("}");
            if (i < blocks.size() - 1) sb.append(",");
            sb.append(nl);
        }
        sb.append(indent1).append("]").append(nl);
        sb.append("}");
        return sb.toString();
    }

    public static class Metadata {
        public String version;
        public String name;
        public Bounds bounds;
        public Dimensions dimensions;
        public long exportedAt;

        public Metadata(String version, int[] min, int[] max, int[] dimensions, String name) {
            this.version = version;
            this.name = name;
            this.bounds = new Bounds(min, max);
            this.dimensions = new Dimensions(dimensions[0], dimensions[1], dimensions[2]);
            this.exportedAt = System.currentTimeMillis();
        }
    }

    public static class Bounds {
        public int[] min;
        public int[] max;

        public Bounds(int[] min, int[] max) {
            this.min = min;
            this.max = max;
        }
    }

    public static class Dimensions {
        public int x;
        public int y;
        public int z;

        public Dimensions(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static class PaletteEntry {
        public int id;
        public String name;
        public Map<String, String> properties;
        public String base64Texture;

        public PaletteEntry(int id, String name, Map<String, String> properties, String base64Texture) {
            this.id = id;
            this.name = name;
            this.properties = properties;
            this.base64Texture = base64Texture;
        }
    }

    public static class BlockEntry {
        public int x;
        public int y;
        public int z;
        public int p; // Palette ID reference

        public BlockEntry(int x, int y, int z, int p) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.p = p;
        }
    }
}
