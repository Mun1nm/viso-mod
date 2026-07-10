import { MinecraftDataService } from './loader/MinecraftData.js';
import { ExportLoader } from './loader/ExportLoader.js';
import { TextureManager } from './engine/TextureManager.js';
import { IsometricScene } from './engine/IsometricScene.js';
import { ChunkRenderer } from './engine/ChunkRenderer.js';
import { RaycastInspector } from './engine/RaycastInspector.js';
import { SampleStructureProvider } from './samples/sampleStructures.js';

class App {
  constructor() {
    this.mcDataService = new MinecraftDataService('1.20.4');
    this.textureManager = new TextureManager(this.mcDataService);

    const container = document.getElementById('canvas-container');
    this.isoScene = new IsometricScene(container);
    this.chunkRenderer = new ChunkRenderer(this.isoScene.scene, this.textureManager, this.mcDataService);
    this.inspector = new RaycastInspector(this.isoScene, this.chunkRenderer, this.mcDataService);

    this.currentStructure = null;
    this.is2DMode = false; // kept in sync for chunkRenderer

    this.initUI();

    const embeddedEl = document.getElementById('embedded-structure-data');
    if (embeddedEl && embeddedEl.textContent && !embeddedEl.textContent.includes('EMBEDDED_STRUCTURE_DATA')) {
      try {
        const rawJson = JSON.parse(embeddedEl.textContent);
        const normalized = ExportLoader.normalizeStructure(rawJson);
        this.loadStructureData(normalized);
      } catch (err) {
        console.error('[VisoMod] Erro ao ler JSON embutido no HTML:', err);
        this.loadSample('castle');
      }
    } else {
      this.loadSample('castle');
    }

    this.animate();
  }

  initUI() {
    // 1. Sample Selection Dropdown (hidden but functional)
    const sampleSelect = document.getElementById('sample-select');
    if (sampleSelect) {
      sampleSelect.addEventListener('change', (e) => {
        this.loadSample(e.target.value);
      });
    }

    // 2. File Upload Button
    const fileInput = document.getElementById('file-input');
    fileInput.addEventListener('change', async (e) => {
      const file = e.target.files[0];
      if (file) {
        try {
          const data = await ExportLoader.loadFromFile(file);
          this.loadStructureData(data);
        } catch (err) {
          alert('Erro ao carregar arquivo de exportação: ' + err.message);
        }
      }
    });

    // 3. Drag and Drop Zone
    const dropZone = document.getElementById('drop-zone');
    window.addEventListener('dragenter', (e) => {
      e.preventDefault();
      dropZone.classList.remove('hidden');
    });

    dropZone.addEventListener('dragover', (e) => e.preventDefault());

    dropZone.addEventListener('dragleave', (e) => {
      if (e.relatedTarget === null || e.target === dropZone) {
        dropZone.classList.add('hidden');
      }
    });

    dropZone.addEventListener('drop', async (e) => {
      e.preventDefault();
      dropZone.classList.add('hidden');
      const file = e.dataTransfer.files[0];
      if (file) {
        try {
          const data = await ExportLoader.loadFromFile(file);
          this.loadStructureData(data);
        } catch (err) {
          alert('Erro na leitura do arquivo GZIP/JSON: ' + err.message);
        }
      }
    });

    // 4. Y-Slice Slider & Navigation Buttons
    this.ySlider = document.getElementById('y-slice-slider');
    this.yLabel = document.getElementById('slice-y-label');
    const btnUp = document.getElementById('btn-slice-up');
    const btnDown = document.getElementById('btn-slice-down');

    this.ySlider.addEventListener('input', (e) => {
      const val = parseInt(e.target.value, 10);
      this.updateYSlice(val);
    });

    btnUp.addEventListener('click', () => {
      const current = parseInt(this.ySlider.value, 10);
      const max = parseInt(this.ySlider.max, 10);
      if (current < max) {
        this.ySlider.value = current + 1;
        this.updateYSlice(current + 1);
      }
    });

    btnDown.addEventListener('click', () => {
      const current = parseInt(this.ySlider.value, 10);
      if (current > 0) {
        this.ySlider.value = current - 1;
        this.updateYSlice(current - 1);
      }
    });

    // 5. Culling Toggle
    const toggleCulling = document.getElementById('toggle-culling');
    toggleCulling.addEventListener('change', (e) => {
      const stats = this.chunkRenderer.setCulling(e.target.checked);
      this.updateStatsUI(stats);
    });

    // 6. Isometric Angle Buttons (All angles available for both modes)
    const allAngleBtns = document.querySelectorAll('.angle-group-wrapper .btn-iso');
    allAngleBtns.forEach(btn => {
      btn.addEventListener('click', () => {
        allAngleBtns.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        this.isoScene.setIsometricAngle(parseInt(btn.dataset.angle, 10));
      });
    });

    // Start in 3D mode at 45°
    this.isoScene.setIsometricAngle(45);

    // 7. Camera Controls
    document.getElementById('btn-zoom-in').addEventListener('click', () => {
      this.isoScene.setZoom(-3);
    });

    document.getElementById('btn-zoom-out').addEventListener('click', () => {
      this.isoScene.setZoom(3);
    });

    document.getElementById('btn-reset-view').addEventListener('click', () => {
      if (this.currentStructure) {
        this.isoScene.centerOnStructure(this.currentStructure.metadata.dimensions);
      }
    });

    // 8. Environment Toggles
    const btnGrid = document.getElementById('btn-toggle-grid');
    btnGrid.addEventListener('click', () => {
      btnGrid.classList.toggle('active');
      this.isoScene.toggleGrid(btnGrid.classList.contains('active'));
    });

    const btnShadows = document.getElementById('btn-toggle-shadows');
    btnShadows.addEventListener('click', () => {
      btnShadows.classList.toggle('active');
      this.isoScene.toggleShadows(btnShadows.classList.contains('active'));
    });

    // 9. Vision Mode Switch (3D <-> 2D)
    const toggleViewMode = document.getElementById('toggle-view-mode');

    if (toggleViewMode) {
      toggleViewMode.addEventListener('change', (e) => {
        this.is2DMode = e.target.checked;
        this.isoScene.set2DMode(this.is2DMode);
        this.chunkRenderer.set2DMode(this.is2DMode); // enables/disables shadow layer

        const toggleSingleLayer = document.getElementById('toggle-single-layer');
        if (shadowStyleContainer && toggleSingleLayer) {
          shadowStyleContainer.style.display = (toggleSingleLayer.checked && this.is2DMode) ? 'block' : 'none';
        }
        
        // Auto-switch to an appropriate default angle ONLY if the user hasn't explicitly
        // chosen one from the other group, or just reset to a nice default for the mode.
        // Let's reset to 0° for 2D and 45° for 3D to keep it intuitive.
        if (this.is2DMode) {
          allAngleBtns.forEach(b => b.classList.remove('active'));
          document.querySelector('.angle-group-2d .btn-iso[data-angle="0"]').classList.add('active');
          this.isoScene.setIsometricAngle(0);
        } else {
          allAngleBtns.forEach(b => b.classList.remove('active'));
          document.querySelector('.angle-group-3d .btn-iso[data-angle="45"]').classList.add('active');
          this.isoScene.setIsometricAngle(45);
        }
      });
    }

    // 10. Single Layer Only Checkbox
    const toggleSingleLayer = document.getElementById('toggle-single-layer');
    const shadowStyleContainer = document.getElementById('shadow-style-container');
    if (toggleSingleLayer) {
      toggleSingleLayer.addEventListener('change', (e) => {
        const isSingleLayer = e.target.checked;
        if (shadowStyleContainer) {
          shadowStyleContainer.style.display = (isSingleLayer && this.is2DMode) ? 'block' : 'none';
        }
        const stats = this.chunkRenderer.setSingleLayerOnly(isSingleLayer);
        this.updateStatsUI(stats);
        this.updateLegendUI();
      });
    }

    // 11. Distinct Colors (High Contrast Legend) Checkbox
    const toggleDistinctColors = document.getElementById('toggle-distinct-colors');
    if (toggleDistinctColors) {
      toggleDistinctColors.addEventListener('change', (e) => {
        const stats = this.chunkRenderer.setDistinctColorsMode(e.target.checked);
        this.updateStatsUI(stats);
        this.updateLegendUI();
      });
    }

    // 12. Shadow Style Dropdown
    const selectShadowStyle = document.getElementById('select-shadow-style');
    if (selectShadowStyle) {
      selectShadowStyle.addEventListener('change', (e) => {
        const stats = this.chunkRenderer.setShadowStyle(e.target.value);
        this.updateStatsUI(stats);
        this.updateLegendUI();
      });
    }
  }

  async loadSample(sampleId) {
    if (sampleId === 'castle') {
      try {
        const norm = await ExportLoader.loadFromUrl('/samples/test_castle.json.gz');
        this.loadStructureData(norm);
        return;
      } catch (e) {
        console.warn('Fallback GZIP falhou, tentando JSON ou procedimental:', e);
        try {
          const normJson = await ExportLoader.loadFromUrl('/samples/test_castle.json');
          this.loadStructureData(normJson);
          return;
        } catch (e2) {
          console.warn('Usando castelo procedimental:', e2);
        }
      }
    }

    const data = SampleStructureProvider.getSample(sampleId);
    this.loadStructureData(data);
  }

  loadStructureData(data) {
    this.currentStructure = data;
    this.mcDataService.registerPalette(data.palette);

    const dims = data.metadata.dimensions;
    const stats = this.chunkRenderer.loadStructure(data);

    // Configure Y Slider bounds
    const maxY = dims.y - 1;
    this.ySlider.max = maxY;
    this.ySlider.value = maxY;
    this.yLabel.textContent = `Y: ${maxY}`;

    // Center scene isometric camera
    this.isoScene.centerOnStructure(dims);

    // Set up block-aligned overlay grid for 2D mode
    this.isoScene.setOverlayGridForStructure(dims, maxY);

    // Update Dimensions UI stat
    document.getElementById('stat-dimensions').textContent = `${dims.x} × ${dims.y} × ${dims.z}`;
    this.updateStatsUI(stats);
    this.updateLegendUI();

    // Update page title and header with structure name
    const structureName = exportData.metadata?.name || exportData.metadata?.fileName || 'Estrutura';
    document.title = `VisoMod — ${structureName}`;
    const headerName = document.getElementById('header-structure-name');
    if (headerName) headerName.textContent = structureName;
  }

  updateYSlice(sliceY) {
    this.yLabel.textContent = `Y: ${sliceY}`;
    const stats = this.chunkRenderer.setSliceY(sliceY);
    // Move overlay grid to follow the current layer in 2D mode
    this.isoScene.setOverlayGridY(sliceY);
    this.updateStatsUI(stats);
    this.updateLegendUI();
  }

  updateLegendUI() {
    const legendPanel = document.getElementById('legend-panel');
    const legendList = document.getElementById('legend-list');
    if (!legendPanel || !legendList) return;

    if (!this.chunkRenderer.distinctColorsMode) {
      legendPanel.classList.add('hidden');
      return;
    }

    legendPanel.classList.remove('hidden');
    const items = this.chunkRenderer.getLegendData();
    legendList.innerHTML = items.map(item => `
      <div class="legend-item">
        <span class="legend-color-badge" style="background-color: ${item.colorHex};"></span>
        <span class="legend-block-name" title="${item.blockId}">${item.displayName}</span>
        <span class="legend-block-count">${item.count}</span>
      </div>
    `).join('');
  }

  updateStatsUI(stats) {
    if (!stats) return;
    document.getElementById('stat-total').textContent = stats.total ?? 0;
    document.getElementById('stat-visible').textContent = stats.visible ?? 0;
    document.getElementById('stat-culled').textContent = stats.culled ?? 0;
  }

  animate() {
    requestAnimationFrame(() => this.animate());
    this.isoScene.render();
  }
}

window.addEventListener('DOMContentLoaded', () => {
  new App();
});
