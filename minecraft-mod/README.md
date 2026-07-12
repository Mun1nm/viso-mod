# Viso-Mod: Minecraft Exporter (Fabric 26.2)

This folder contains the Java source code for the Minecraft Fabric mod that handles selecting regions in the world and extracting the geometry, textures, and block states into a highly optimized JSON payload.

## Development Setup

1. **Prerequisites:**
   - Java 21+
   - Minecraft 26.2 (Fabric API 26.2)

2. **Building the Mod:**
   To compile the `.jar` file yourself, run the following Gradle wrapper command in your terminal:
   ```bash
   ./gradlew build
   ```
   The built `.jar` will be placed in `build/libs/`.

3. **Running the Development Client:**
   To test the mod directly in a temporary Minecraft instance:
   ```bash
   ./gradlew runClient
   ```

## Key Components
- `ExportWandItem`: The tool used to define the A and B bounds of your export region.
- `StructureExporter`: The core logic that loops over the region and extracts blocks.
- `EntityModelExtractor`: Extracts custom geometry and layered textures for complex entities like Banners, Chests, and Skulls.
