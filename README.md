<div align="center">
  <h1>Viso-Mod</h1>
  <p><b>Mine-to-Web Isometric Exporter for Minecraft (Fabric 1.21.4)</b></p>
  
  ![Fabric](https://img.shields.io/badge/Fabric-1.21.4-blue?style=for-the-badge)
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

*(Replace these placeholders with your actual screenshots/GIFs before posting!)*

![In-game Selection](https://via.placeholder.com/800x400.png?text=In-Game+Selection+Wand)
![Web Viewer Demo](https://via.placeholder.com/800x400.png?text=Web+Viewer+Isometric+View)
![Slice Feature Demo](https://via.placeholder.com/800x400.png?text=Layer-by-Layer+Slicing+Feature)

---

## 🚀 Quick Start

Viso-Mod is split into two parts: the **Minecraft Mod** (to export your builds) and the **Web Viewer** (to see them in 3D in your browser).

### 1. The Minecraft Mod (Exporting)
Download the latest `.jar` from the [Releases folder](releases/) and place it in your `mods/` directory (requires Fabric for Minecraft 1.21.4 - v26.2).

**How to export a structure:**
1. Grab the **Export Wand** from your creative inventory (Tools tab).
2. **Left-Click** a block to set Point A.
3. **Right-Click** a block to set Point B.
4. Type `/exportar <name>` in chat (e.g., `/exportar my_castle`).
5. Your structure is saved at `run/exports/my_castle.json.gz`!

### 2. The Web Viewer (Viewing)
The web viewer is a lightweight Three.js + Vite app.

1. Open the `web-viewer/` folder in your terminal.
2. Install dependencies and start the local server:
   ```bash
   npm install
   npm run dev
   ```
3. Open `http://localhost:3000` in your browser.
4. Drag and drop your `.json.gz` file into the viewer and enjoy!

---

## 🛠️ Project Structure & Development

Want to contribute or tinker with the code? Check out the specific READMEs in each folder:

- [Minecraft Mod Source & Build Instructions](./minecraft-mod/README.md)
- [Web Viewer Source & Setup Instructions](./web-viewer/README.md)

---

## 📝 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
