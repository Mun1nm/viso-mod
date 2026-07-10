import * as THREE from 'three';
import { mergeGeometries } from 'three/examples/jsm/utils/BufferGeometryUtils.js';

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
    this.geometryCache = new Map();

    this.shadowMeshes = [];
    this.shadowMaterialCache = new Map();
    this.shadowStyle = 'opacity';
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
    this.shadowMaterialCache.clear();
    return this.rebuildMeshes();
  }

  setShadowStyle(style) {
    this.shadowStyle = style;
    this.shadowMaterialCache.clear();
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
      
      const info = this.mcDataService.getBlockInfo(blockId);
      const texData = this.distinctColorsMode
        ? { materials: [this.getDistinctMaterialForBlock(blockId)], texMap: null }
        : this.textureManager.getMaterialForBlock(blockId);

      const materials = texData.materials;
      const texMap = texData.texMap;

      const geometry = this.getGeometryForBlock(info, texMap);

      const instMesh = new THREE.InstancedMesh(geometry, materials, instances.length);
      instMesh.castShadow = true;
      instMesh.receiveShadow = true;
      instMesh.userData = { blockId };

      for (let i = 0; i < instances.length; i++) {
        const b = instances[i];
        dummy.position.set(b.x, b.y, b.z);
        dummy.rotation.set(0, 0, 0);
        
        // Apply rotations based on facing/axis properties ONLY for procedural blocks
        if (info.properties && !info.model) {
            const facing = info.properties.facing;
            const axis = info.properties.axis;
            if (facing) {
                if (facing === 'east') dummy.rotation.y = -Math.PI / 2;
                else if (facing === 'west') dummy.rotation.y = Math.PI / 2;
                else if (facing === 'south') dummy.rotation.y = Math.PI;
                else if (facing === 'up') dummy.rotation.x = -Math.PI / 2;
                else if (facing === 'down') dummy.rotation.x = Math.PI / 2;
            } else if (axis) {
                if (axis === 'x') dummy.rotation.z = Math.PI / 2;
                else if (axis === 'z') dummy.rotation.x = Math.PI / 2;
            }
        }
        
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
    const key = blockId + "_" + this.distinctColorsMode + "_" + this.shadowStyle;
    if (!this.shadowMaterialCache.has(key)) {
      const originalData = this.distinctColorsMode
        ? { materials: [this.getDistinctMaterialForBlock(blockId)] }
        : this.textureManager.getMaterialForBlock(blockId);
      
      const original = originalData.materials;
      
      const cloneMaterial = (m) => {
        const c = m.clone();
        c.transparent = true;
        c.depthWrite = false;
        c.depthTest = false; // ensures visibility over background and ignores occlusion
        
        switch (this.shadowStyle) {
          case 'wireframe':
            c.wireframe = true;
            c.opacity = 0.5;
            break;
          case 'tint':
            c.color.setHex(0x1a2d45);
            c.opacity = 0.7;
            break;
          case 'shrink':
            c.opacity = 0.45;
            break;
          case 'opacity':
          default:
            c.opacity = 0.35;
            break;
        }
        return c;
      };
      
      const shadow = Array.isArray(original) 
        ? original.map(cloneMaterial)
        : [cloneMaterial(original)];
        
      this.shadowMaterialCache.set(key, shadow);
    }
    return this.shadowMaterialCache.get(key);
  }

  _buildShadowLayer(dummy) {
    const shadowY = this.currentSliceY - 1;

    // Build a quick lookup for blocks at the current layer
    const blocksAtCurrentY = new Set();
    for (const b of this.allBlocks) {
      if (b.y === this.currentSliceY) {
        blocksAtCurrentY.add(`${b.x},${b.z}`);
      }
    }

    // Collect all blocks at shadowY that are NOT covered by a block at currentSliceY
    const shadowBlocks = this.allBlocks.filter(b => b.y === shadowY && !blocksAtCurrentY.has(`${b.x},${b.z}`));
    if (shadowBlocks.length === 0) return;

    // Group by blockId for efficient instancing
    const byBlock = new Map();
    for (const b of shadowBlocks) {
      if (!byBlock.has(b.id)) byBlock.set(b.id, []);
      byBlock.get(b.id).push(b);
    }

    for (const [blockId, instances] of byBlock.entries()) {
      if (instances.length === 0) continue;

      const info = this.mcDataService.getBlockInfo(blockId);
      const geometry = this.getGeometryForBlock(info);
      const shadowMat = this.getShadowMaterial(blockId);
      const shadowMesh = new THREE.InstancedMesh(geometry, shadowMat, instances.length);
      shadowMesh.castShadow = false;
      shadowMesh.receiveShadow = false;
      shadowMesh.renderOrder = -1; // render before main layer

      for (let i = 0; i < instances.length; i++) {
        const b = instances[i];
        dummy.position.set(b.x, b.y, b.z);
        dummy.rotation.set(0, 0, 0);
        
        // Apply rotations based on facing/axis properties ONLY for procedural blocks
        if (info.properties && !info.model) {
            const facing = info.properties.facing;
            const axis = info.properties.axis;
            if (facing) {
                if (facing === 'east') dummy.rotation.y = -Math.PI / 2;
                else if (facing === 'west') dummy.rotation.y = Math.PI / 2;
                else if (facing === 'south') dummy.rotation.y = Math.PI;
                else if (facing === 'up') dummy.rotation.x = -Math.PI / 2;
                else if (facing === 'down') dummy.rotation.x = Math.PI / 2;
            } else if (axis) {
                if (axis === 'x') dummy.rotation.z = Math.PI / 2;
                else if (axis === 'z') dummy.rotation.x = Math.PI / 2;
            }
        }
        if (this.shadowStyle === 'shrink') {
          dummy.scale.set(0.85, 0.85, 0.85);
        } else {
          dummy.scale.set(1, 1, 1);
        }
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
    if (info.transparent) return false;
    // Slabs, stairs, walls are never fully opaque cubes
    if (info.name && (info.name.includes('slab') || info.name.includes('stairs') || info.name.includes('wall') || info.name.includes('fence'))) {
      return false;
    }
    return true;
  }

  getGeometryForBlock(info, texMap) {
    const cacheKey = info.name + (info.properties ? JSON.stringify(info.properties) : "") + (info.model ? 'model' : '');
    if (this.geometryCache.has(cacheKey)) {
        return this.geometryCache.get(cacheKey);
    }
    
    let geom;

    if (info.model && info.model.quads && info.model.quads.length > 0) {
        geom = new THREE.BufferGeometry();
        const positions = [];
        const uvs = [];
        const indices = [];
        
        let vIndex = 0;
        let lastTex = null;
        let groupStart = 0;
        let groupCount = 0;

        for (const q of info.model.quads) {
            // Apply Minecraft space (-X, Y, -Z) to ThreeJS space by translating by -0.5 on each axis to center block at (0,0,0)
            for (let i = 0; i < 4; i++) {
                positions.push(q.pos[i*3] - 0.5, q.pos[i*3+1] - 0.5, q.pos[i*3+2] - 0.5);
                uvs.push(q.uv[i*2], q.uv[i*2+1]);
            }
            
            // Standard quad triangulation: 0,1,2 and 0,2,3
            indices.push(vIndex, vIndex + 1, vIndex + 2);
            indices.push(vIndex, vIndex + 2, vIndex + 3);
            
            if (q.texture !== lastTex && lastTex !== null) {
                const matIndex = (texMap && texMap[lastTex] !== undefined) ? texMap[lastTex] : 0;
                geom.addGroup(groupStart * 3, groupCount * 3, matIndex);
                groupStart += groupCount;
                groupCount = 0;
            }
            
            lastTex = q.texture;
            groupCount += 2; // 2 triangles
            vIndex += 4;
        }
        
        if (groupCount > 0) {
            const matIndex = (texMap && texMap[lastTex] !== undefined) ? texMap[lastTex] : 0;
            geom.addGroup(groupStart * 3, groupCount * 3, matIndex);
        }

        geom.setAttribute('position', new THREE.Float32BufferAttribute(positions, 3));
        geom.setAttribute('uv', new THREE.Float32BufferAttribute(uvs, 2));
        geom.setIndex(indices);
        geom.computeVertexNormals();

    } else {
        geom = new THREE.BoxGeometry(1, 1, 1);
        if (info.name && (info.name.includes('slab') || info.name.includes('carpet') || info.name.includes('snow') || info.name.includes('door') || info.name.includes('trapdoor') || info.name.includes('stairs'))) {
            // Fallbacks for when model is not available or extracted
            if (info.name.includes('slab')) {
                const type = info.properties ? info.properties.type : 'bottom';
                if (type === 'bottom') geom.translate(0, -0.25, 0);
            }
        }
    }
    
    this.geometryCache.set(cacheKey, geom);
    return geom;
  }

  fixExtrudeUVs(geometry) {
    const pos = geometry.attributes.position;
    const uvs = geometry.attributes.uv;
    for (let i = 0; i < pos.count; i++) {
        let x = pos.getX(i);
        let y = pos.getY(i);
        let z = pos.getZ(i);
        // Simple planar UV mapping
        uvs.setXY(i, x + 0.5, y + 0.5);
    }
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
