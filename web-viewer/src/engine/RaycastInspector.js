import * as THREE from 'three';

export class RaycastInspector {
  constructor(isometricScene, chunkRenderer, mcDataService) {
    this.isoScene = isometricScene;
    this.chunkRenderer = chunkRenderer;
    this.mcDataService = mcDataService;

    this.raycaster = new THREE.Raycaster();
    this.mouse = new THREE.Vector2();

    // Highlight Box Helper
    const highlightGeo = new THREE.BoxGeometry(1.02, 1.02, 1.02);
    const highlightMat = new THREE.MeshBasicMaterial({
      color: 0x00f0ff,
      wireframe: true,
      transparent: true,
      opacity: 0.9
    });
    this.highlightMesh = new THREE.Mesh(highlightGeo, highlightMat);
    this.highlightMesh.visible = false;
    this.isoScene.scene.add(this.highlightMesh);

    // UI elements
    this.cardEl = document.getElementById('inspector-card');
    this.nameEl = document.getElementById('inspector-block-name');
    this.typeEl = document.getElementById('inspector-block-type');
    this.colorEl = document.getElementById('inspector-color-preview');
    this.coordX = document.getElementById('coord-x');
    this.coordY = document.getElementById('coord-y');
    this.coordZ = document.getElementById('coord-z');

    this.setupListeners();
  }

  setupListeners() {
    const container = this.isoScene.container;

    container.addEventListener('mousemove', (e) => {
      const rect = container.getBoundingClientRect();
      this.mouse.x = ((e.clientX - rect.left) / rect.width) * 2 - 1;
      this.mouse.y = -((e.clientY - rect.top) / rect.height) * 2 + 1;

      this.inspectAtCursor();
    });

    container.addEventListener('mouseleave', () => {
      this.hideInspector();
    });
  }

  inspectAtCursor() {
    this.raycaster.setFromCamera(this.mouse, this.isoScene.camera);

    const meshes = Array.from(this.chunkRenderer.instancedMeshes.values());
    const intersects = this.raycaster.intersectObjects(meshes, false);

    if (intersects.length > 0) {
      const hit = intersects[0];
      const mesh = hit.object;
      const instanceId = hit.instanceId;
      const blockId = mesh.userData.blockId;

      const instances = this.chunkRenderer.blockInstances.get(blockId);
      if (instances && instances[instanceId]) {
        const b = instances[instanceId];

        // Move highlight bounding box to block coordinates
        this.highlightMesh.position.set(b.x, b.y, b.z);
        this.highlightMesh.visible = true;

        this.updateInspectorUI(blockId, b);
        return;
      }
    }

    this.hideInspector();
  }

  updateInspectorUI(blockId, b) {
    const info = this.mcDataService.getBlockInfo(blockId);

    this.cardEl.classList.remove('hidden');
    this.nameEl.textContent = info.name;
    this.typeEl.textContent = info.transparent ? 'Bloco Translúcido' : 'Bloco Sólido';
    this.colorEl.style.backgroundColor = info.baseColor;

    this.coordX.textContent = b.x;
    this.coordY.textContent = b.y;
    this.coordZ.textContent = b.z;
  }

  hideInspector() {
    this.highlightMesh.visible = false;
    this.cardEl.classList.add('hidden');
  }
}
