<div align="center">
  <h1>Viso-Mod</h1>
  <p><b>Mine-to-Web Isometric Exporter for Minecraft (Fabric 26.2)</b></p>
  
  ![Fabric](https://img.shields.io/badge/Fabric-26.2-blue?style=for-the-badge)
  ![Three.js](https://img.shields.io/badge/Three.js-WebGL-black?style=for-the-badge)
  ![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)
</div>

<br/>

> **Viso-Mod** is an integrated toolset that lets you export any 3D region of your Minecraft world into an ultra-compact JSON file, and view it in a high-performance **interactive isometric web viewer**.

---

## ✨ Features
- **In-Game Selection Wand:** Easily select any region using the Export Wand (Points A and B).
- **Ultra-Compact Export:** Extracts blocks, optimized textures, block states, and complex entity models (Chests, Skulls, Decorated Banners, Copper Golems) straight into a `.json.gz` file.
- **Distinct Colors & Tints:** Accurately extracts dynamic tints from biomes, redstone power levels, water flow, and dyed banners.
- **Web Viewer (Three.js):** 
  - True 35.264° orthographic isometric camera.
  - Rotate the structure in 90° increments.
  - **Dynamic Y-Slicer:** Slice through your builds layer by layer using a slider (perfect for inspecting interiors!).
  - **InstancedMesh Rendering:** 60 FPS performance even on massive structures. Hidden interior blocks are culled automatically!
  - **Interactive Raycasting:** Hover over any block to see its ID, coordinates, and properties.

---

## 📷 Screenshots / Demo


![In-game Selection](<img width="426" height="240" alt="gif1" src="https://github.com/user-attachments/assets/761f9859-2f6f-49d0-bbef-61cb814896f0" />)
![Web Viewer Demo](<img width="426" height="240" alt="gif2" src="https://github.com/user-attachments/assets/afa710db-aa35-44d2-9bdc-0d89215deb03" />)
![Slice Feature Demo](<img width="426" height="240" alt="gif3" src="https://github.com/user-attachments/assets/c2f04d21-5064-4697-b044-c0d6a9859cb4" />)

---

## 🚀 Quick Start

Viso-Mod is split into two parts: the **Minecraft Mod** (to export your builds) and the **Online Web Viewer** (to see them in 3D in your browser).

### 1. The Minecraft Mod (Exporting)
Download the latest `.jar` from the **[Releases Page](https://github.com/Mun1nm/viso-mod/releases/latest)** and place it in your `mods/` directory (requires Fabric for Minecraft 26.2).

**How to export a structure:**
1. Grab the **Export Wand** from your creative inventory (Tools tab).
2. **Left-Click** a block to set Point A.
3. **Right-Click** a block to set Point B.
4. Type `/export <name>` in chat (e.g., `/export my_castle`).
5. Your structure is saved at `run/exports/my_castle.json.gz`!

> **Developer Tip:** If you want to see the raw text data instead of a compressed `.gz` file for debugging, you can use the command `/exportdebug <name>`. It generates a plain, uncompressed `.json` file!

### 2. The Web Viewer (Viewing)
You don't need to install anything to view your exports! Simply open the official hosted web viewer:

👉 **[Launch Web Viewer](https://mun1nm.github.io/viso-mod/)**

Once opened, just drag and drop your exported `.json.gz` (or `.json`) file into the browser window and enjoy!

---

## 🛠️ Project Structure & Development

Want to contribute, tinker with the code, or run the web viewer locally? Check out the specific READMEs in each folder:

- [Minecraft Mod Source & Build Instructions](./minecraft-mod/README.md)
- [Web Viewer Source & Local Setup Instructions](./web-viewer/README.md)

---

## 📝 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
