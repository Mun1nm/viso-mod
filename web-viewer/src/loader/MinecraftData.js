export class MinecraftDataService {
  constructor(version = '26.2') {
    this.version = version;
    this.blockCache = new Map();
    this.registry = this.buildBlockRegistry();
    this.palette = {};
  }

  registerPalette(palette) {
    this.palette = palette || {};
    this.blockCache.clear();
  }

  getBlockInfo(rawId) {
    const cleanId = rawId.replace('minecraft:', '');
    if (this.blockCache.has(cleanId)) {
      return this.blockCache.get(cleanId);
    }

    let baseId = cleanId;
    let properties = null;
    let base64Texture = null;
    let shapes = null;
    let displayName = cleanId.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());

    if (cleanId.startsWith('palette_')) {
        const pKey = cleanId.substring(8);
        const pEntry = this.palette[pKey];
        if (pEntry) {
            baseId = pEntry.name.replace('minecraft:', '');
            properties = pEntry.properties;
            base64Texture = pEntry.base64Texture;
            shapes = pEntry.shapes;
            displayName = baseId.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());
            if (properties) {
                const propsStr = Object.entries(properties).map(([k,v]) => `${k}=${v}`).join(', ');
                if (propsStr) displayName += ` [${propsStr}]`;
            }
        }
    }

    const reg = this.registry[baseId];
    const info = {
      id: rawId,
      name: baseId,
      displayName: displayName,
      hardness: reg ? reg.hardness : 1.5,
      material: reg ? reg.material : 'rock',
      transparent: reg ? reg.transparent : (baseId.includes('glass') || baseId.includes('leaves') || baseId.includes('water')),
      isWater: baseId.includes('water'),
      isGlass: baseId.includes('glass'),
      isLeaves: baseId.includes('leaves'),
      baseColor: this.resolveBaseColor(baseId),
      properties: properties,
      base64Texture: base64Texture,
      shapes: shapes
    };

    this.blockCache.set(cleanId, info);
    return info;
  }

  buildBlockRegistry() {
    return {
      stone: { name: 'Stone', hardness: 1.5, material: 'rock', transparent: false },
      cobblestone: { name: 'Cobblestone', hardness: 2.0, material: 'rock', transparent: false },
      stone_bricks: { name: 'Stone Bricks', hardness: 1.5, material: 'rock', transparent: false },
      grass_block: { name: 'Grass Block', hardness: 0.6, material: 'dirt', transparent: false },
      dirt: { name: 'Dirt', hardness: 0.5, material: 'dirt', transparent: false },
      oak_log: { name: 'Oak Log', hardness: 2.0, material: 'wood', transparent: false },
      oak_planks: { name: 'Oak Planks', hardness: 2.0, material: 'wood', transparent: false },
      spruce_planks: { name: 'Spruce Planks', hardness: 2.0, material: 'wood', transparent: false },
      birch_planks: { name: 'Birch Planks', hardness: 2.0, material: 'wood', transparent: false },
      glass: { name: 'Glass', hardness: 0.3, material: 'glass', transparent: true },
      water: { name: 'Water', hardness: 100.0, material: 'water', transparent: true },
      leaves: { name: 'Oak Leaves', hardness: 0.2, material: 'plant', transparent: true },
      oak_leaves: { name: 'Oak Leaves', hardness: 0.2, material: 'plant', transparent: true },
      bricks: { name: 'Bricks', hardness: 2.0, material: 'rock', transparent: false },
      gold_block: { name: 'Block of Gold', hardness: 3.0, material: 'metal', transparent: false },
      iron_block: { name: 'Block of Iron', hardness: 5.0, material: 'metal', transparent: false },
      diamond_block: { name: 'Block of Diamond', hardness: 5.0, material: 'metal', transparent: false },
      coal_ore: { name: 'Coal Ore', hardness: 3.0, material: 'rock', transparent: false }
    };
  }

  resolveBaseColor(cleanId) {
    const colorMap = {
      stone: '#7d7d7d',
      cobblestone: '#696969',
      grass_block: '#5c8f38',
      dirt: '#866043',
      oak_log: '#6d5032',
      oak_planks: '#b8945f',
      spruce_planks: '#755436',
      birch_planks: '#d7c185',
      glass: '#a8e6cf',
      water: '#3f76e4',
      leaves: '#366e2c',
      oak_leaves: '#38782a',
      bricks: '#9c4d44',
      stone_bricks: '#6c6c6c',
      gold_block: '#f9d342',
      iron_block: '#dcdcdc',
      diamond_block: '#63eed2',
      sand: '#dfd5a5',
      gravel: '#767271',
      coal_ore: '#4e4e4e'
    };

    if (colorMap[cleanId]) return colorMap[cleanId];

    if (cleanId.includes('wood') || cleanId.includes('log')) return '#6d5032';
    if (cleanId.includes('plank')) return '#b8945f';
    if (cleanId.includes('leaf') || cleanId.includes('leaves')) return '#38782a';
    if (cleanId.includes('stone') || cleanId.includes('slate')) return '#7d7d7d';
    if (cleanId.includes('ore')) return '#6f7478';
    if (cleanId.includes('brick')) return '#9c4d44';

    return '#8a9ba8';
  }
}
