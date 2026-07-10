import * as THREE from 'three';

export class TextureManager {
  constructor(mcDataService) {
    this.mcDataService = mcDataService;
    this.materialCache = new Map();
  }

  getMaterialForBlock(blockId) {
    if (this.materialCache.has(blockId)) {
      return this.materialCache.get(blockId);
    }

    const info = this.mcDataService.getBlockInfo(blockId);
    let result = { materials: [], texMap: {} };

    if (info.model && info.model.textures) {
      let index = 0;
      for (const [texName, base64] of Object.entries(info.model.textures)) {
        const img = new Image();
        img.src = base64;
        const tex = new THREE.Texture(img);
        img.onload = () => { tex.needsUpdate = true; };
        tex.magFilter = THREE.NearestFilter;
        tex.minFilter = THREE.NearestFilter;
        tex.colorSpace = THREE.SRGBColorSpace;
        
        // Fix UV mapping vertically because Minecraft UVs have V pointing down, 
        // while ThreeJS has V pointing up.
        tex.wrapS = THREE.RepeatWrapping;
        tex.wrapT = THREE.RepeatWrapping;
        tex.flipY = false; 

        const mat = this.makeStandardMat(tex, info.transparent);
        result.materials.push(mat);
        result.texMap[texName] = index++;
      }
    } else if (info.base64Texture) {
      const img = new Image();
      img.src = info.base64Texture;
      const tex = new THREE.Texture(img);
      img.onload = () => { tex.needsUpdate = true; };
      tex.magFilter = THREE.NearestFilter;
      tex.minFilter = THREE.NearestFilter;
      tex.colorSpace = THREE.SRGBColorSpace;
      tex.flipY = false;
      const mat = this.makeStandardMat(tex, info.transparent);
      result.materials = [mat, mat, mat, mat, mat, mat];
    } else {
      result.materials = this.generateBlockMaterials(blockId, info);
    }
    
    this.materialCache.set(blockId, result);
    return result;
  }

  generateBlockMaterials(blockId, info) {
    const baseColor = info.baseColor || '#7d7d7d';

    if (blockId.includes('grass_block')) {
      // Top green grass, bottom dirt, sides dirt with top grass border
      const topTex = this.createCanvasTexture('#5c8f38', 'grass_top');
      const bottomTex = this.createCanvasTexture('#866043', 'dirt');
      const sideTex = this.createCanvasTexture('#866043', 'grass_side');

      return [
        this.makeStandardMat(sideTex),   // right (+x)
        this.makeStandardMat(sideTex),   // left (-x)
        this.makeStandardMat(topTex),    // top (+y)
        this.makeStandardMat(bottomTex), // bottom (-y)
        this.makeStandardMat(sideTex),   // front (+z)
        this.makeStandardMat(sideTex)    // back (-z)
      ];
    }

    if (blockId.includes('oak_log') || blockId.includes('_log')) {
      const topTex = this.createCanvasTexture('#9e8054', 'log_top');
      const sideTex = this.createCanvasTexture('#6d5032', 'log_side');
      return [
        this.makeStandardMat(sideTex),
        this.makeStandardMat(sideTex),
        this.makeStandardMat(topTex),
        this.makeStandardMat(topTex),
        this.makeStandardMat(sideTex),
        this.makeStandardMat(sideTex)
      ];
    }

    // Default 6 identical faces with pixel art texture
    const tex = this.createCanvasTexture(baseColor, this.getPatternType(blockId));
    const mat = this.makeStandardMat(tex, info.transparent);
    return [mat, mat, mat, mat, mat, mat];
  }

  getPatternType(blockId) {
    if (blockId.includes('stone') || blockId.includes('ore')) return 'stone';
    if (blockId.includes('cobble')) return 'cobble';
    if (blockId.includes('plank')) return 'planks';
    if (blockId.includes('brick')) return 'bricks';
    if (blockId.includes('glass')) return 'glass';
    if (blockId.includes('water')) return 'water';
    if (blockId.includes('leaves')) return 'leaves';
    return 'noise';
  }

  makeStandardMat(texture, transparent = false) {
    return new THREE.MeshStandardMaterial({
      map: texture,
      roughness: 0.82,
      metalness: 0.08,
      transparent: transparent,
      opacity: transparent ? 0.65 : 1.0,
      side: THREE.FrontSide
    });
  }

  createCanvasTexture(baseHex, pattern) {
    const canvas = document.createElement('canvas');
    canvas.width = 16;
    canvas.height = 16;
    const ctx = canvas.getContext('2d');

    // Parse base color
    ctx.fillStyle = baseHex;
    ctx.fillRect(0, 0, 16, 16);

    const rgb = this.hexToRgb(baseHex);

    // Apply pixel art shading based on pattern
    for (let y = 0; y < 16; y++) {
      for (let x = 0; x < 16; x++) {
        let variance = 0;

        if (pattern === 'stone' || pattern === 'noise' || pattern === 'dirt') {
          variance = ((x * 17 + y * 31) % 5) - 2;
        } else if (pattern === 'cobble') {
          if ((x % 4 === 0) || (y % 4 === 0)) variance = -40;
          else variance = ((x * 7 + y * 13) % 4) * 8 - 12;
        } else if (pattern === 'planks') {
          if (y % 4 === 0) variance = -35;
          else if (x % 8 === 0 && y % 2 === 0) variance = -25;
          else variance = ((x + y) % 3) * 6 - 8;
        } else if (pattern === 'bricks') {
          if (y % 4 === 0 || ((y / 4 | 0) % 2 === 0 ? x % 8 === 0 : (x + 4) % 8 === 0)) variance = -45;
          else variance = ((x * 11 + y * 7) % 3) * 8;
        } else if (pattern === 'grass_side') {
          if (y < 3 || (y === 3 && x % 2 === 0)) {
            ctx.fillStyle = '#5c8f38';
            ctx.fillRect(x, y, 1, 1);
            continue;
          } else {
            variance = ((x * 17 + y * 31) % 5) - 2;
          }
        } else if (pattern === 'glass') {
          if (x === 0 || y === 0 || x === 15 || y === 15 || (x + y === 6)) {
            ctx.fillStyle = 'rgba(255,255,255,0.75)';
            ctx.fillRect(x, y, 1, 1);
          }
          continue;
        }

        if (variance !== 0) {
          const nr = Math.min(255, Math.max(0, rgb.r + variance * 6));
          const ng = Math.min(255, Math.max(0, rgb.g + variance * 6));
          const nb = Math.min(255, Math.max(0, rgb.b + variance * 6));
          ctx.fillStyle = `rgb(${nr},${ng},${nb})`;
          ctx.fillRect(x, y, 1, 1);
        }
      }
    }

    const texture = new THREE.CanvasTexture(canvas);
    texture.magFilter = THREE.NearestFilter;
    texture.minFilter = THREE.NearestFilter;
    texture.colorSpace = THREE.SRGBColorSpace;
    return texture;
  }

  hexToRgb(hex) {
    let clean = hex.replace('#', '');
    if (clean.length === 3) {
      clean = clean[0]+clean[0]+clean[1]+clean[1]+clean[2]+clean[2];
    }
    const bigint = parseInt(clean, 16);
    return {
      r: (bigint >> 16) & 255,
      g: (bigint >> 8) & 255,
      b: bigint & 255
    };
  }
}
