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

  toggleGrid(show) {
    this.gridHelper.visible = show;
  }

  toggleShadows(show) {
    this.sun.castShadow = show;
  }

  setIsometricAngle(deg) {
    this.currentAngleDeg = deg;
    this.updateCameraPosition();
  }

  setZoom(delta) {
    this.zoom = Math.max(4, Math.min(80, this.zoom + delta));
    this.updateCameraProjection();
  }

  centerOnStructure(dimensions) {
    // Center point is half of dimensions
    this.targetCenter.set(
      (dimensions.x - 1) / 2,
      (dimensions.y - 1) / 4,
      (dimensions.z - 1) / 2
    );

    this.gridHelper.position.set(this.targetCenter.x, -0.55, this.targetCenter.z);

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
