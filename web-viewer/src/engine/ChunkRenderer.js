import * as THREE from 'three';

export class ChunkRenderer {
  constructor(scene, textureManager, mcDataService) {
    this.scene = scene;
    this.textureManager = textureManager;
    this.mcDataService = mcDataService;

    this.instancedMeshes = new Map(); // blockId -> InstancedMesh
    this.blockInstances = new Map();  // blockId -> array of visible BlockEntry
    this.allBlocks = [];
    this.voxelGrid = new Map();       // "x,y,z" -> BlockEntry

    this.currentSliceY = 999;
    this.enableCulling = true;

    this.geometry = new THREE.BoxGeometry(1, 1, 1);
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

  rebuildMeshes() {
    // Remove existing meshes from scene
    for (const mesh of this.instancedMeshes.values()) {
      this.scene.remove(mesh);
      mesh.dispose();
    }
    this.instancedMeshes.clear();
    this.blockInstances.clear();

    let totalVisible = 0;
    let totalCulled = 0;

    // Group non-culled blocks within Y slice by block ID
    for (const b of this.allBlocks) {
      if (b.y > this.currentSliceY) {
        continue;
      }

      if (this.enableCulling && this.isBlockFullyOccluded(b)) {
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

      const materials = this.textureManager.getMaterialForBlock(blockId);
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

    // Return render stats
    return {
      total: this.allBlocks.length,
      visible: totalVisible,
      culled: totalCulled
    };
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
    this.allBlocks = [];
    this.voxelGrid.clear();
  }
}
