package com.visomod.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class HtmlExporter {

    public static File generateStandaloneHtml(File exportsDir, String fileName, String jsonContent) throws IOException {
        File htmlFile = new File(exportsDir, fileName + ".html");
        try (FileOutputStream fos = new FileOutputStream(htmlFile);
             OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            writer.write(getHtmlTemplate(fileName, jsonContent));
        }
        return htmlFile;
    }

    private static String getHtmlTemplate(String title, String jsonContent) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"pt-BR\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>VisoMod 3D Viewer - " + escapeHtml(title) + "</title>\n" +
                "  <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n" +
                "  <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>\n" +
                "  <link href=\"https://fonts.googleapis.com/css2?family=Outfit:wght@400;600;700&family=JetBrains+Mono:wght@400;600&display=swap\" rel=\"stylesheet\">\n" +
                "  <script src=\"https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js\"></script>\n" +
                "  <style>\n" +
                "    :root {\n" +
                "      --bg-gradient: radial-gradient(circle at 50% 30%, #1e293b 0%, #0f172a 60%, #020617 100%);\n" +
                "      --glass-bg: rgba(15, 23, 42, 0.75);\n" +
                "      --glass-border: rgba(255, 255, 255, 0.12);\n" +
                "      --accent: #38bdf8;\n" +
                "      --accent-glow: rgba(56, 189, 248, 0.35);\n" +
                "      --text-main: #f8fafc;\n" +
                "      --text-dim: #94a3b8;\n" +
                "    }\n" +
                "    * { box-sizing: border-box; margin: 0; padding: 0; }\n" +
                "    body {\n" +
                "      font-family: 'Outfit', sans-serif;\n" +
                "      background: var(--bg-gradient);\n" +
                "      color: var(--text-main);\n" +
                "      overflow: hidden;\n" +
                "      width: 100vw;\n" +
                "      height: 100vh;\n" +
                "      user-select: none;\n" +
                "    }\n" +
                "    #canvas-container {\n" +
                "      position: absolute;\n" +
                "      top: 0; left: 0; width: 100%; height: 100%;\n" +
                "      cursor: grab;\n" +
                "    }\n" +
                "    #canvas-container:active { cursor: grabbing; }\n" +
                "    .glass-panel {\n" +
                "      background: var(--glass-bg);\n" +
                "      backdrop-filter: blur(16px);\n" +
                "      -webkit-backdrop-filter: blur(16px);\n" +
                "      border: 1px solid var(--glass-border);\n" +
                "      border-radius: 16px;\n" +
                "      box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);\n" +
                "    }\n" +
                "    header {\n" +
                "      position: absolute;\n" +
                "      top: 24px; left: 24px;\n" +
                "      padding: 16px 24px;\n" +
                "      display: flex;\n" +
                "      align-items: center;\n" +
                "      gap: 16px;\n" +
                "      z-index: 10;\n" +
                "    }\n" +
                "    .logo-badge {\n" +
                "      width: 42px; height: 42px;\n" +
                "      border-radius: 12px;\n" +
                "      background: linear-gradient(135deg, #0284c7, #38bdf8);\n" +
                "      display: flex; align-items: center; justify-content: center;\n" +
                "      font-weight: 700; font-size: 20px; color: #fff;\n" +
                "      box-shadow: 0 0 20px var(--accent-glow);\n" +
                "    }\n" +
                "    header h1 {\n" +
                "      font-size: 20px; font-weight: 700; letter-spacing: -0.02em;\n" +
                "    }\n" +
                "    header p {\n" +
                "      font-size: 12px; color: var(--text-dim);\n" +
                "    }\n" +
                "    #stats-panel {\n" +
                "      position: absolute;\n" +
                "      top: 24px; right: 24px;\n" +
                "      padding: 18px 24px;\n" +
                "      z-index: 10;\n" +
                "      min-width: 220px;\n" +
                "    }\n" +
                "    .stat-row {\n" +
                "      display: flex; justify-content: space-between; align-items: center;\n" +
                "      margin-bottom: 8px;\n" +
                "      font-size: 13px;\n" +
                "    }\n" +
                "    .stat-row:last-child { margin-bottom: 0; }\n" +
                "    .stat-val {\n" +
                "      font-family: 'JetBrains Mono', monospace;\n" +
                "      font-weight: 600;\n" +
                "      color: var(--accent);\n" +
                "    }\n" +
                "    #controls-bar {\n" +
                "      position: absolute;\n" +
                "      bottom: 24px; left: 50%;\n" +
                "      transform: translateX(-50%);\n" +
                "      padding: 12px 20px;\n" +
                "      display: flex; align-items: center; gap: 16px;\n" +
                "      z-index: 10;\n" +
                "    }\n" +
                "    .btn-group { display: flex; gap: 8px; }\n" +
                "    .btn {\n" +
                "      background: rgba(255, 255, 255, 0.08);\n" +
                "      border: 1px solid rgba(255, 255, 255, 0.15);\n" +
                "      color: var(--text-main);\n" +
                "      padding: 8px 14px;\n" +
                "      border-radius: 10px;\n" +
                "      font-family: 'Outfit', sans-serif;\n" +
                "      font-weight: 600;\n" +
                "      font-size: 13px;\n" +
                "      cursor: pointer;\n" +
                "      transition: all 0.2s ease;\n" +
                "    }\n" +
                "    .btn:hover {\n" +
                "      background: var(--accent);\n" +
                "      color: #0f172a;\n" +
                "      box-shadow: 0 0 15px var(--accent-glow);\n" +
                "      transform: translateY(-1px);\n" +
                "    }\n" +
                "    .slider-container {\n" +
                "      display: flex; align-items: center; gap: 12px;\n" +
                "      border-left: 1px solid rgba(255, 255, 255, 0.15);\n" +
                "      padding-left: 16px;\n" +
                "    }\n" +
                "    .slider-container label { font-size: 12px; font-weight: 600; color: var(--text-dim); }\n" +
                "    input[type=range] {\n" +
                "      accent-color: var(--accent);\n" +
                "      cursor: pointer;\n" +
                "    }\n" +
                "    #tooltip {\n" +
                "      position: absolute;\n" +
                "      pointer-events: none;\n" +
                "      padding: 10px 16px;\n" +
                "      display: none;\n" +
                "      z-index: 20;\n" +
                "      transform: translate(-50%, -130%);\n" +
                "    }\n" +
                "    #tooltip h4 { font-size: 14px; font-weight: 700; color: #fff; margin-bottom: 4px; }\n" +
                "    #tooltip p { font-family: 'JetBrains Mono', monospace; font-size: 12px; color: var(--accent); }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div id=\"canvas-container\"></div>\n" +
                "\n" +
                "  <header class=\"glass-panel\">\n" +
                "    <div class=\"logo-badge\">V</div>\n" +
                "    <div>\n" +
                "      <h1>" + escapeHtml(title) + "</h1>\n" +
                "      <p>VisoMod Standalone Isometric Viewer</p>\n" +
                "    </div>\n" +
                "  </header>\n" +
                "\n" +
                "  <div id=\"stats-panel\" class=\"glass-panel\">\n" +
                "    <div class=\"stat-row\"><span>Blocos:</span><span id=\"stat-blocks\" class=\"stat-val\">0</span></div>\n" +
                "    <div class=\"stat-row\"><span>Dimensões:</span><span id=\"stat-dims\" class=\"stat-val\">0×0×0</span></div>\n" +
                "    <div class=\"stat-row\"><span>Paleta:</span><span id=\"stat-palette\" class=\"stat-val\">0</span></div>\n" +
                "  </div>\n" +
                "\n" +
                "  <div id=\"controls-bar\" class=\"glass-panel\">\n" +
                "    <div class=\"btn-group\">\n" +
                "      <button class=\"btn\" onclick=\"rotateScene(1)\">↻ Girar 90°</button>\n" +
                "      <button class=\"btn\" onclick=\"resetCamera()\">⌂ Centralizar</button>\n" +
                "    </div>\n" +
                "    <div class=\"slider-container\">\n" +
                "      <label>Camada Y:</label>\n" +
                "      <input type=\"range\" id=\"slice-y\" min=\"0\" max=\"1\" value=\"1\" step=\"1\" oninput=\"updateSlice(this.value)\">\n" +
                "      <span id=\"slice-val\" class=\"stat-val\">All</span>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "\n" +
                "  <div id=\"tooltip\" class=\"glass-panel\">\n" +
                "    <h4 id=\"tooltip-name\">Block Name</h4>\n" +
                "    <p id=\"tooltip-pos\">X: 0 | Y: 0 | Z: 0</p>\n" +
                "  </div>\n" +
                "\n" +
                "  <script id=\"structure-data\" type=\"application/json\">\n" +
                jsonContent + "\n" +
                "  </script>\n" +
                "\n" +
                "  <script>\n" +
                "    let scene, camera, renderer, instancedGroup, selectionBox;\n" +
                "    let currentData = null;\n" +
                "    let currentAngleIndex = 0;\n" +
                "    const ANGLES = [Math.PI / 4, 3 * Math.PI / 4, -3 * Math.PI / 4, -Math.PI / 4];\n" +
                "    let structureCenter = new THREE.Vector3();\n" +
                "    let cameraDistance = 40;\n" +
                "    let isDragging = false, prevMouse = { x: 0, y: 0 };\n" +
                "    let raycaster = new THREE.Raycaster();\n" +
                "    let mouse = new THREE.Vector2();\n" +
                "    let blockInstanceMap = [];\n" +
                "\n" +
                "    const COLOR_MAP = {\n" +
                "      'grass_block': 0x589c3e, 'dirt': 0x866043, 'stone': 0x7f7f7f, 'cobblestone': 0x6e6e6e,\n" +
                "      'oak_log': 0x6b5130, 'oak_leaves': 0x3a7a24, 'oak_planks': 0xa2824e, 'water': 0x3f76e4,\n" +
                "      'sand': 0xdbd3a0, 'glass': 0xaadcff, 'brick': 0x965341, 'default': 0x94a3b8\n" +
                "    };\n" +
                "\n" +
                "    function init() {\n" +
                "      const container = document.getElementById('canvas-container');\n" +
                "      scene = new THREE.Scene();\n" +
                "\n" +
                "      const aspect = window.innerWidth / window.innerHeight;\n" +
                "      const d = 25;\n" +
                "      camera = new THREE.OrthographicCamera(-d * aspect, d * aspect, d, -d, 1, 1000);\n" +
                "\n" +
                "      renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });\n" +
                "      renderer.setSize(window.innerWidth, window.innerHeight);\n" +
                "      renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));\n" +
                "      container.appendChild(renderer.domElement);\n" +
                "\n" +
                "      const ambient = new THREE.AmbientLight(0xffffff, 0.65);\n" +
                "      scene.add(ambient);\n" +
                "      const sun = new THREE.DirectionalLight(0xfffbeb, 0.85);\n" +
                "      sun.position.set(30, 50, 40);\n" +
                "      scene.add(sun);\n" +
                "      const fill = new THREE.DirectionalLight(0x38bdf8, 0.3);\n" +
                "      fill.position.set(-30, 20, -30);\n" +
                "      scene.add(fill);\n" +
                "\n" +
                "      selectionBox = new THREE.LineSegments(\n" +
                "        new THREE.EdgesGeometry(new THREE.BoxGeometry(1.02, 1.02, 1.02)),\n" +
                "        new THREE.LineBasicMaterial({ color: 0x38bdf8, linewidth: 2 })\n" +
                "      );\n" +
                "      selectionBox.visible = false;\n" +
                "      scene.add(selectionBox);\n" +
                "\n" +
                "      setupEvents();\n" +
                "      loadEmbeddedStructure();\n" +
                "      animate();\n" +
                "    }\n" +
                "\n" +
                "    function getBlockColor(blockId) {\n" +
                "      for (let key in COLOR_MAP) {\n" +
                "        if (blockId.includes(key)) return COLOR_MAP[key];\n" +
                "      }\n" +
                "      let hash = 0;\n" +
                "      for (let i = 0; i < blockId.length; i++) hash = blockId.charCodeAt(i) + ((hash << 5) - hash);\n" +
                "      const c = (hash & 0x00FFFFFF).toString(16).toUpperCase();\n" +
                "      return parseInt('00000'.substring(0, 6 - c.length) + c, 16);\n" +
                "    }\n" +
                "\n" +
                "    function loadEmbeddedStructure() {\n" +
                "      const raw = document.getElementById('structure-data').textContent;\n" +
                "      if (!raw || !raw.trim()) return;\n" +
                "      currentData = JSON.parse(raw);\n" +
                "      renderStructure(currentData, currentData.dimensions[1] - 1);\n" +
                "\n" +
                "      document.getElementById('stat-blocks').innerText = currentData.blocks.length.toLocaleString();\n" +
                "      const dims = currentData.dimensions;\n" +
                "      document.getElementById('stat-dims').innerText = dims[0] + '×' + dims[1] + '×' + dims[2];\n" +
                "      document.getElementById('stat-palette').innerText = Object.keys(currentData.palette).length;\n" +
                "\n" +
                "      const slider = document.getElementById('slice-y');\n" +
                "      slider.max = dims[1] - 1;\n" +
                "      slider.value = dims[1] - 1;\n" +
                "      document.getElementById('slice-val').innerText = 'All';\n" +
                "    }\n" +
                "\n" +
                "    function renderStructure(data, maxY) {\n" +
                "      if (instancedGroup) scene.remove(instancedGroup);\n" +
                "      instancedGroup = new THREE.Group();\n" +
                "      blockInstanceMap = [];\n" +
                "\n" +
                "      const geom = new THREE.BoxGeometry(1, 1, 1);\n" +
                "      const byPalette = {};\n" +
                "\n" +
                "      data.blocks.forEach(b => {\n" +
                "        if (b.y > maxY) return;\n" +
                "        if (!byPalette[b.paletteId]) byPalette[b.paletteId] = [];\n" +
                "        byPalette[b.paletteId].push(b);\n" +
                "      });\n" +
                "\n" +
                "      Object.keys(byPalette).forEach(pId => {\n" +
                "        const entry = data.palette[pId];\n" +
                "        const name = entry ? entry.name : 'unknown';\n" +
                "        const colorHex = getBlockColor(name);\n" +
                "        const mat = new THREE.MeshLambertMaterial({ color: colorHex });\n" +
                "\n" +
                "        const blocks = byPalette[pId];\n" +
                "        const mesh = new THREE.InstancedMesh(geom, mat, blocks.length);\n" +
                "        const matrix = new THREE.Matrix4();\n" +
                "\n" +
                "        blocks.forEach((b, idx) => {\n" +
                "          matrix.setPosition(b.x, b.y, b.z);\n" +
                "          mesh.setMatrixAt(idx, matrix);\n" +
                "          blockInstanceMap.push({ mesh, instanceId: idx, b, name });\n" +
                "        });\n" +
                "\n" +
                "        instancedGroup.add(mesh);\n" +
                "      });\n" +
                "\n" +
                "      scene.add(instancedGroup);\n" +
                "      const dims = data.dimensions;\n" +
                "      structureCenter.set((dims[0] - 1) / 2, (dims[1] - 1) / 2, (dims[2] - 1) / 2);\n" +
                "      cameraDistance = Math.max(dims[0], dims[1], dims[2]) * 1.5;\n" +
                "      updateCameraPosition();\n" +
                "    }\n" +
                "\n" +
                "    function updateCameraPosition() {\n" +
                "      const theta = ANGLES[currentAngleIndex];\n" +
                "      const phi = Math.atan(1 / Math.sqrt(2));\n" +
                "      const x = structureCenter.x + cameraDistance * Math.cos(phi) * Math.sin(theta);\n" +
                "      const y = structureCenter.y + cameraDistance * Math.sin(phi);\n" +
                "      const z = structureCenter.z + cameraDistance * Math.cos(phi) * Math.cos(theta);\n" +
                "      camera.position.set(x, y, z);\n" +
                "      camera.lookAt(structureCenter);\n" +
                "      camera.updateProjectionMatrix();\n" +
                "    }\n" +
                "\n" +
                "    function rotateScene(dir) {\n" +
                "      currentAngleIndex = (currentAngleIndex + dir + ANGLES.length) % ANGLES.length;\n" +
                "      updateCameraPosition();\n" +
                "    }\n" +
                "\n" +
                "    function resetCamera() {\n" +
                "      currentAngleIndex = 0;\n" +
                "      updateCameraPosition();\n" +
                "    }\n" +
                "\n" +
                "    function updateSlice(val) {\n" +
                "      if (!currentData) return;\n" +
                "      const maxY = parseInt(val);\n" +
                "      document.getElementById('slice-val').innerText = (maxY === currentData.dimensions[1] - 1) ? 'All' : 'Y: ' + maxY;\n" +
                "      renderStructure(currentData, maxY);\n" +
                "    }\n" +
                "\n" +
                "    function setupEvents() {\n" +
                "      window.addEventListener('resize', () => {\n" +
                "        const aspect = window.innerWidth / window.innerHeight;\n" +
                "        const d = 25;\n" +
                "        camera.left = -d * aspect; camera.right = d * aspect;\n" +
                "        camera.top = d; camera.bottom = -d;\n" +
                "        camera.updateProjectionMatrix();\n" +
                "        renderer.setSize(window.innerWidth, window.innerHeight);\n" +
                "      });\n" +
                "\n" +
                "      const el = document.getElementById('canvas-container');\n" +
                "      el.addEventListener('mousedown', e => { isDragging = true; prevMouse = { x: e.clientX, y: e.clientY }; });\n" +
                "      window.addEventListener('mouseup', () => isDragging = false);\n" +
                "      window.addEventListener('mousemove', e => {\n" +
                "        if (isDragging) {\n" +
                "          const dx = e.clientX - prevMouse.x;\n" +
                "          const dy = e.clientY - prevMouse.y;\n" +
                "          structureCenter.x -= (dx * Math.cos(ANGLES[currentAngleIndex]) + dy * Math.sin(ANGLES[currentAngleIndex])) * 0.05;\n" +
                "          structureCenter.z -= (-dx * Math.sin(ANGLES[currentAngleIndex]) + dy * Math.cos(ANGLES[currentAngleIndex])) * 0.05;\n" +
                "          updateCameraPosition();\n" +
                "          prevMouse = { x: e.clientX, y: e.clientY };\n" +
                "        }\n" +
                "        handleHover(e);\n" +
                "      });\n" +
                "\n" +
                "      el.addEventListener('wheel', e => {\n" +
                "        camera.zoom = Math.max(0.2, Math.min(5.0, camera.zoom * (e.deltaY > 0 ? 0.9 : 1.1)));\n" +
                "        camera.updateProjectionMatrix();\n" +
                "      });\n" +
                "    }\n" +
                "\n" +
                "    function handleHover(e) {\n" +
                "      mouse.x = (e.clientX / window.innerWidth) * 2 - 1;\n" +
                "      mouse.y = -(e.clientY / window.innerHeight) * 2 + 1;\n" +
                "      raycaster.setFromCamera(mouse, camera);\n" +
                "\n" +
                "      const tooltip = document.getElementById('tooltip');\n" +
                "      if (!instancedGroup) return;\n" +
                "      const hits = raycaster.intersectObjects(instancedGroup.children);\n" +
                "      if (hits.length > 0) {\n" +
                "        const hit = hits[0];\n" +
                "        const info = blockInstanceMap.find(m => m.mesh === hit.object && m.instanceId === hit.instanceId);\n" +
                "        if (info) {\n" +
                "          selectionBox.position.set(info.b.x, info.b.y, info.b.z);\n" +
                "          selectionBox.visible = true;\n" +
                "          tooltip.style.display = 'block';\n" +
                "          tooltip.style.left = e.clientX + 'px';\n" +
                "          tooltip.style.top = e.clientY + 'px';\n" +
                "          document.getElementById('tooltip-name').innerText = info.name.replace('minecraft:', '');\n" +
                "          document.getElementById('tooltip-pos').innerText = `X: ${info.b.x} | Y: ${info.b.y} | Z: ${info.b.z}`;\n" +
                "          return;\n" +
                "        }\n" +
                "      }\n" +
                "      selectionBox.visible = false;\n" +
                "      tooltip.style.display = 'none';\n" +
                "    }\n" +
                "\n" +
                "    window.addEventListener('DOMContentLoaded', init);\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>";
    }

    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
