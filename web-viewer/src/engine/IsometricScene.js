import * as THREE from 'three';

export class IsometricScene {
  constructor(container) {
    this.container = container;
    this.width = container.clientWidth;
    this.height = container.clientHeight;

    this.scene = new THREE.Scene();

    // Orthographic Isometric Camera
    const aspect = this.width / this.height;
    this.zoom = 18;
    this.camera = new THREE.OrthographicCamera(
      -this.zoom * aspect,
      this.zoom * aspect,
      this.zoom,
      -this.zoom,
      -500,
      1000
    );

    this.currentAngleDeg = 45; // Classic Isometric NE
    this.targetCenter = new THREE.Vector3(0, 0, 0);

    this.is2DMode = false;
    this._gridVisible = true; // tracks user's grid preference

    // Renderer setup
    this.renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
    this.renderer.setSize(this.width, this.height);
    this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    this.renderer.shadowMap.enabled = true;
    this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
    this.renderer.toneMapping = THREE.ACESFilmicToneMapping;
    this.renderer.toneMappingExposure = 1.15;
    container.appendChild(this.renderer.domElement);

    this.setupLights();
    this.setupFloorGrid();
    this.setupOverlayGrid();
    this.setupInteraction();

    this.updateCameraPosition();

    // Resize observer
    window.addEventListener('resize', () => this.onResize());
  }

  setupLights() {
    // Soft Ambient Light
    const ambient = new THREE.AmbientLight(0xffffff, 0.55);
    this.scene.add(ambient);

    // Hemisphere Light (Sky cyan / Ground indigo)
    const hemi = new THREE.HemisphereLight(0x8bcbee, 0x1d2838, 0.45);
    this.scene.add(hemi);

    // Sun Directional Light
    this.sun = new THREE.DirectionalLight(0xfffaed, 1.4);
    this.sun.position.set(25, 45, 30);
    this.sun.castShadow = true;
    this.sun.shadow.mapSize.width = 2048;
    this.sun.shadow.mapSize.height = 2048;
    this.sun.shadow.camera.near = -50;
    this.sun.shadow.camera.far = 150;
    const d = 30;
    this.sun.shadow.camera.left = -d;
    this.sun.shadow.camera.right = d;
    this.sun.shadow.camera.top = d;
    this.sun.shadow.camera.bottom = -d;
    this.sun.shadow.bias = -0.0005;

    this.scene.add(this.sun);
  }

  setupFloorGrid() {
    this.gridHelper = new THREE.GridHelper(60, 60, 0x00f0ff, 0x1f2937);
    this.gridHelper.position.y = -0.55;
    this.scene.add(this.gridHelper);
  }

  /**
   * Overlay grid for 2D top-down mode: 1-unit-per-block grid rendered ABOVE blocks.
   * Aligned to block edges so each cell = 1 Minecraft block.
   * Starts hidden; shown when is2DMode=true and _gridVisible=true.
   */
  setupOverlayGrid() {
    // Start with a default size; resized to structure on load
    this.overlayGrid = this._makeOverlayGrid(60, 60, 0, 0, 0.51);
    this.overlayGrid.visible = false;
    this.scene.add(this.overlayGrid);
  }

  _makeOverlayGrid(width, depth, cx, cz, y) {
    // Use 1-unit divisions so every cell maps to exactly 1 block
    const sizeX = Math.max(width, 1);
    const sizeZ = Math.max(depth, 1);
    const size = Math.max(sizeX, sizeZ);

    const grid = new THREE.GridHelper(size, size, 0xffffff, 0xffffff);
    grid.material.opacity = 0.18;
    grid.material.transparent = true;
    // Shift so grid lines fall on block EDGES (not centers)
    // Block at (0,y,0) occupies [-0.5..+0.5]. Edges at -0.5 and +0.5.
    // GridHelper(N,N) centered at cx puts first line at cx - N/2.
    // We want first line at -0.5, so cx = -0.5 + N/2 = (N-1)/2 = (width-1)/2. ✓
    grid.position.set(cx, y, cz);
    return grid;
  }

  /** Resize & reposition the overlay grid to match the loaded structure. */
  setOverlayGridForStructure(dimensions, sliceY) {
    const size = Math.max(dimensions.x, dimensions.z);
    // Align the square GridHelper so its first line falls exactly on -0.5 for both axes
    const cx = (size - 1) / 2;
    const cz = (size - 1) / 2;

    // Dispose old, create new
    this.scene.remove(this.overlayGrid);
    this.overlayGrid.geometry.dispose();
    this.overlayGrid.material.dispose();

    this.overlayGrid = this._makeOverlayGrid(dimensions.x, dimensions.z, cx, cz, sliceY + 0.51);
    this.overlayGrid.visible = this.is2DMode && this._gridVisible;
    this.scene.add(this.overlayGrid);
  }

  /** Move overlay grid to sit just above the given Y layer. */
  setOverlayGridY(y) {
    if (this.overlayGrid) {
      this.overlayGrid.position.y = y + 0.51;
    }
  }

  toggleGrid(show) {
    this._gridVisible = show;
    if (this.is2DMode) {
      // In 2D mode: toggle the block-aligned overlay grid
      this.overlayGrid.visible = show;
    } else {
      // In 3D mode: toggle the floor grid
      this.gridHelper.visible = show;
    }
  }

  toggleShadows(show) {
    this.sun.castShadow = show;
  }

  setIsometricAngle(deg) {
    this.currentAngleDeg = deg;
    this.updateCameraPosition();
  }

  set2DMode(is2D) {
    this.is2DMode = is2D;
    if (is2D) {
      // Switch from floor grid → overlay grid
      this.gridHelper.visible = false;
      this.overlayGrid.visible = this._gridVisible;
    } else {
      // Switch from overlay grid → floor grid
      this.overlayGrid.visible = false;
      this.gridHelper.visible = this._gridVisible;
    }
    this.updateCameraPosition();
  }

  setZoom(delta) {
    this.zoom = Math.max(4, Math.min(80, this.zoom + delta));
    this.updateCameraProjection();
  }

  centerOnStructure(dimensions) {
    // Center point is half of dimensions
    this.targetCenter = new THREE.Vector3(
      (dimensions.x - 1) / 2,
      (dimensions.y - 1) / 2,
      (dimensions.z - 1) / 2
    );

    // Floor grid lines must fall on half-integers (-0.5, 0.5...) to frame blocks correctly.
    // Since GridHelper(60,60) draws lines at integer offsets from its center,
    // its center must be placed at a half-integer coordinate.
    const gridX = Math.floor(this.targetCenter.x) + 0.5;
    const gridZ = Math.floor(this.targetCenter.z) + 0.5;
    this.gridHelper.position.set(gridX, -0.55, gridZ);

    // Auto-fit zoom based on structure bounding radius
    const maxDim = Math.max(dimensions.x, dimensions.y, dimensions.z);
    this.zoom = Math.max(8, maxDim * 0.95);

    this.updateCameraProjection();
    this.updateCameraPosition();
  }

  updateCameraProjection() {
    const aspect = this.width / this.height;
    this.camera.left = -this.zoom * aspect;
    this.camera.right = this.zoom * aspect;
    this.camera.top = this.zoom;
    this.camera.bottom = -this.zoom;
    this.camera.updateProjectionMatrix();
  }

  updateCameraPosition() {
    const rad = (this.currentAngleDeg * Math.PI) / 180;

    if (this.is2DMode) {
      const distance = 80;
      this.camera.up.set(-Math.sin(rad), 0, -Math.cos(rad));
      this.camera.position.set(
        this.targetCenter.x,
        this.targetCenter.y + distance,
        this.targetCenter.z
      );
      this.camera.lookAt(this.targetCenter);
      return;
    }

    this.camera.up.set(0, 1, 0);
    const distance = 80;

    // Classic true isometric vertical elevation angle ~35.264 degrees
    const isoElevation = Math.atan(1 / Math.sqrt(2));
    const yOffset = distance * Math.sin(isoElevation);
    const xzDist = distance * Math.cos(isoElevation);

    const xOffset = xzDist * Math.sin(rad);
    const zOffset = xzDist * Math.cos(rad);

    this.camera.position.set(
      this.targetCenter.x + xOffset,
      this.targetCenter.y + yOffset,
      this.targetCenter.z + zOffset
    );
    this.camera.lookAt(this.targetCenter);
  }

  setupInteraction() {
    let isDragging = false;
    let prevX = 0, prevY = 0;

    this.container.addEventListener('mousedown', (e) => {
      if (e.button === 0) { // Left click pan
        isDragging = true;
        prevX = e.clientX;
        prevY = e.clientY;
      }
    });

    window.addEventListener('mouseup', () => {
      isDragging = false;
    });

    window.addEventListener('mousemove', (e) => {
      if (!isDragging) return;
      const dx = e.clientX - prevX;
      const dy = e.clientY - prevY;
      prevX = e.clientX;
      prevY = e.clientY;

      // Pan camera in screen space
      const factor = (this.zoom * 2) / this.height;
      const right = new THREE.Vector3(1, 0, 0).applyQuaternion(this.camera.quaternion);
      const up = new THREE.Vector3(0, 1, 0).applyQuaternion(this.camera.quaternion);

      this.targetCenter.addScaledVector(right, -dx * factor);
      this.targetCenter.addScaledVector(up, dy * factor);

      this.updateCameraPosition();
    });

    this.container.addEventListener('wheel', (e) => {
      e.preventDefault();
      const delta = e.deltaY > 0 ? 1.5 : -1.5;
      this.setZoom(delta);
    }, { passive: false });

    // Touch support for Pan and Pinch-Zoom
    let initialPinchDist = null;
    let initialZoom = null;

    this.container.addEventListener('touchstart', (e) => {
      if (e.touches.length === 1) {
        isDragging = true;
        prevX = e.touches[0].clientX;
        prevY = e.touches[0].clientY;
      } else if (e.touches.length === 2) {
        isDragging = false;
        const dx = e.touches[0].clientX - e.touches[1].clientX;
        const dy = e.touches[0].clientY - e.touches[1].clientY;
        initialPinchDist = Math.sqrt(dx * dx + dy * dy);
        initialZoom = this.zoom;
      }
    }, { passive: false });

    window.addEventListener('touchend', () => {
      isDragging = false;
      initialPinchDist = null;
    });

    window.addEventListener('touchmove', (e) => {
      if (e.touches.length === 1 && isDragging) {
        e.preventDefault();
        const dx = e.touches[0].clientX - prevX;
        const dy = e.touches[0].clientY - prevY;
        prevX = e.touches[0].clientX;
        prevY = e.touches[0].clientY;

        const factor = (this.zoom * 2) / this.height;
        const right = new THREE.Vector3(1, 0, 0).applyQuaternion(this.camera.quaternion);
        const up = new THREE.Vector3(0, 1, 0).applyQuaternion(this.camera.quaternion);
        this.targetCenter.addScaledVector(right, -dx * factor);
        this.targetCenter.addScaledVector(up, dy * factor);
        this.updateCameraPosition();
      } else if (e.touches.length === 2 && initialPinchDist !== null) {
        e.preventDefault();
        const dx = e.touches[0].clientX - e.touches[1].clientX;
        const dy = e.touches[0].clientY - e.touches[1].clientY;
        const dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) {
          const zoomFactor = initialPinchDist / dist;
          this.zoom = Math.max(4, Math.min(80, initialZoom * zoomFactor));
          this.updateCameraProjection();
        }
      }
    }, { passive: false });
  }

  onResize() {
    this.width = this.container.clientWidth;
    this.height = this.container.clientHeight;
    this.renderer.setSize(this.width, this.height);
    this.updateCameraProjection();
  }

  render() {
    this.renderer.render(this.scene, this.camera);
  }
}
