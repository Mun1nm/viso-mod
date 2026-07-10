import * as THREE from 'three';

const HIGH_CONTRAST_PALETTE = [
  '#FF2A6D', '#05D9E8', '#00FF66', '#FFB800', '#B800FF', '#FF5E00',
  '#00E5FF', '#FF6B6B', '#9B5DE5', '#F15BB5', '#00F5D4', '#4361EE',
  '#D90429', '#4CC9F0', '#F8961E', '#2EC4B6', '#F72585', '#7209B7',
  '#3A86FF', '#FB5607', '#8338EC', '#FF006E', '#38B000', '#CCFF33',
  '#FF9E00', '#9D4EDD', '#4895EF', '#560BAD', '#F1A208', '#06D6A0'
];

export class ChunkRenderer {
  constructor(scene, textureManager, mcDataService) {
    this.scene = scene;
    this.textureManager = textureManager;
    this.mcDataService = mcDataService;

    this.instancedMeshes = new Map(); // blockId -> InstancedMesh
    this.blockInstances = new Map();  // blockId -> array of visible BlockEntry
    this.allBlocks = [];
    this.voxelGrid = new Map();       // packed key -> BlockEntry

    this.currentSliceY = 999;
    this.enableCulling = true;
    this.singleLayerOnly = false;
    this.distinctColorsMode = false;
    this.is2DMode = false;  // used to decide if shadow layer should be shown

    this.blockColorMap = new Map();
    this.distinctMaterials = new Map();

    this.geometry = new THREE.BoxGeometry(1, 1, 1);

    this.shadowMeshes = [];
    this.shadowMaterialCache = new Map();
  }

  packCoord(x, y, z) {
    return ((x + 4096) * 16777216) + ((y + 4096) * 4096) + (z + 4096);
  }

  loadStructure(exportData) {
    this.clear();
    this.allBlocks = exportData.blocks;

    // Populate voxel map using integer keys for ultra-fast neighbor culling lookup
    this.voxelGrid.clear();
    for (const b of this.allBlocks) {
      this.voxelGrid.set(this.packCoord(b.x, b.y, b.z), b);
    }

    const dims = exportData.metadata.dimensions;
    this.currentSliceY = dims.y - 1;

    return this.rebuildMeshes();
  }

  setSliceY(maxLayerY) {
    this.currentSliceY = maxLayerY;
    return this.rebuildMeshes();
  }

  setCulling(enabled) {
    this.enableCulling = enabled;
    return this.rebuildMeshes();
  }

  setSingleLayerOnly(enabled) {
    this.singleLayerOnly = enabled;
    return this.rebuildMeshes();
  }

  setDistinctColorsMode(enabled) {
    this.distinctColorsMode = enabled;
    return this.rebuildMeshes();
  }

  set2DMode(enabled) {
    this.is2DMode = enabled;
    return this.rebuildMeshes();
  }

  getDistinctColorHex(blockId) {
    if (!this.blockColorMap.has(blockId)) {
      const idx = this.blockColorMap.size % HIGH_CONTRAST_PALETTE.length;
      this.blockColorMap.set(blockId, HIGH_CONTRAST_PALETTE[idx]);
    }
    return this.blockColorMap.get(blockId);
  }

  getDistinctMaterialForBlock(blockId) {
    if (!this.distinctMaterials.has(blockId)) {
      const hex = this.getDistinctColorHex(blockId);
      const mat = new THREE.MeshLambertMaterial({
        color: new THREE.Color(hex)
      });
      this.distinctMaterials.set(blockId, mat);
    }
    return this.distinctMaterials.get(blockId);
  }

  getLegendData() {
    const list = [];
    for (const [blockId, instances] of this.blockInstances.entries()) {
      if (instances.length === 0) continue;
      const info = this.mcDataService.getBlockInfo(blockId);
      const hex = this.getDistinctColorHex(blockId);
      list.push({
        blockId,
        displayName: info.displayName || blockId,
        colorHex: hex,
        count: instances.length
      });
    }
    list.sort((a, b) => b.count - a.count);
    return list;
  }

  rebuildMeshes() {
    // Remove existing instanced meshes
    for (const mesh of this.instancedMeshes.values()) {
      this.scene.remove(mesh);
      mesh.dispose();
    }
    this.instancedMeshes.clear();
    this.blockInstances.clear();

    // Remove existing shadow meshes
    for (const mesh of this.shadowMeshes) {
      this.scene.remove(mesh);
    }
    this.shadowMeshes = [];

    let totalVisible = 0;
    let totalCulled = 0;

    // Group non-culled blocks within Y slice by block ID
    for (const b of this.allBlocks) {
      if (this.singleLayerOnly) {
        if (b.y !== this.currentSliceY) continue;
      } else {
        if (b.y > this.currentSliceY) continue;
      }

      // In singleLayerOnly mode, we skip culling so interior floor tiles are visible
      if (this.enableCulling && !this.singleLayerOnly && this.isBlockFullyOccluded(b)) {
        totalCulled++;
        continue;
      }

      totalVisible++;
      if (!this.blockInstances.has(b.id)) {
        this.blockInstances.set(b.id, []);
      }
      this.blockInstances.get(b.id).push(b);
    }

    // Build InstancedMesh for each block ID
    const dummy = new THREE.Object3D();

    for (const [blockId, instances] of this.blockInstances.entries()) {
      if (instances.length === 0) continue;

      const materials = this.distinctColorsMode
        ? this.getDistinctMaterialForBlock(blockId)
        : this.textureManager.getMaterialForBlock(blockId);

      const instMesh = new THREE.InstancedMesh(this.geometry, materials, instances.length);
      instMesh.castShadow = true;
      instMesh.receiveShadow = true;
      instMesh.userData = { blockId };

      for (let i = 0; i < instances.length; i++) {
        const b = instances[i];
        dummy.position.set(b.x, b.y, b.z);
        dummy.updateMatrix();
        instMesh.setMatrixAt(i, dummy.matrix);
      }

      instMesh.instanceMatrix.needsUpdate = true;
      this.scene.add(instMesh);
      this.instancedMeshes.set(blockId, instMesh);
    }

    // Shadow layer: only active in 2D + single-layer mode when there's a layer below
    if (this.singleLayerOnly && this.is2DMode && this.currentSliceY > 0) {
      this._buildShadowLayer(dummy);
    }

    // Return render stats
    return {
      total: this.allBlocks.length,
      visible: totalVisible,
      culled: totalCulled
    };
  }

  getShadowMaterial(blockId) {
    const key = blockId + "_" + this.distinctColorsMode;
    if (!this.shadowMaterialCache.has(key)) {
      const original = this.distinctColorsMode
        ? this.getDistinctMaterialForBlock(blockId)
        : this.textureManager.getMaterialForBlock(blockId);
      
      const cloneMaterial = (m) => {
        const c = m.clone();
        c.transparent = true;
        c.opacity = 0.35;
        c.depthWrite = false;
        c.depthTest = false; // ensures visibility over background and ignores occlusion
        return c;
      };
      
      const shadow = Array.isArray(original) 
        ? original.map(cloneMaterial)
        : cloneMaterial(original);
        
      this.shadowMaterialCache.set(key, shadow);
    }
    return this.shadowMaterialCache.get(key);
  }

  _buildShadowLayer(dummy) {
    const shadowY = this.currentSliceY - 1;

    // Collect all blocks at shadowY (no culling — show everything)
    const shadowBlocks = this.allBlocks.filter(b => b.y === shadowY);
    if (shadowBlocks.length === 0) return;

    // Group by blockId for efficient instancing
    const byBlock = new Map();
    for (const b of shadowBlocks) {
      if (!byBlock.has(b.id)) byBlock.set(b.id, []);
      byBlock.get(b.id).push(b);
    }

    for (const [blockId, instances] of byBlock.entries()) {
      if (instances.length === 0) continue;

      const shadowMat = this.getShadowMaterial(blockId);
      const shadowMesh = new THREE.InstancedMesh(this.geometry, shadowMat, instances.length);
      shadowMesh.castShadow = false;
      shadowMesh.receiveShadow = false;
      shadowMesh.renderOrder = -1; // render before main layer

      for (let i = 0; i < instances.length; i++) {
        const b = instances[i];
        dummy.position.set(b.x, b.y, b.z);
        dummy.updateMatrix();
        shadowMesh.setMatrixAt(i, dummy.matrix);
      }

      shadowMesh.instanceMatrix.needsUpdate = true;
      this.scene.add(shadowMesh);
      this.shadowMeshes.push(shadowMesh);
    }
  }

  isBlockFullyOccluded(b) {
    // Se o bloco estiver diretamente abaixo do plano de corte Y, a face superior dele está visível
    if (b.y + 1 > this.currentSliceY) return false;

    // Verificação inlined super rápida dos 6 vizinhos (+x, -x, +y, -y, +z, -z)
    return (
      this.isOpaqueNeighbor(b.x + 1, b.y, b.z) &&
      this.isOpaqueNeighbor(b.x - 1, b.y, b.z) &&
      this.isOpaqueNeighbor(b.x, b.y + 1, b.z) &&
      this.isOpaqueNeighbor(b.x, b.y - 1, b.z) &&
      this.isOpaqueNeighbor(b.x, b.y, b.z + 1) &&
      this.isOpaqueNeighbor(b.x, b.y, b.z - 1)
    );
  }

  isOpaqueNeighbor(x, y, z) {
    const nb = this.voxelGrid.get(this.packCoord(x, y, z));
    if (!nb) return false;
    const info = this.mcDataService.getBlockInfo(nb.id);
    return !info.transparent;
  }

  clear() {
    for (const mesh of this.instancedMeshes.values()) {
      this.scene.remove(mesh);
      mesh.dispose();
    }
    this.instancedMeshes.clear();
    this.blockInstances.clear();
    for (const mesh of this.shadowMeshes) {
      this.scene.remove(mesh);
    }
    this.shadowMeshes = [];
    if (this.shadowMaterialCache) {
      for (const mats of this.shadowMaterialCache.values()) {
        if (Array.isArray(mats)) mats.forEach(m => m.dispose());
        else mats.dispose();
      }
      this.shadowMaterialCache.clear();
    }
    this.allBlocks = [];
    this.voxelGrid.clear();
  }
}
