export class SampleStructureProvider {

  static getSample(sampleId) {
    switch (sampleId) {
      case 'medieval':
        return this.createMedievalHouse();
      case 'mine':
        return this.createUndergroundMine();
      case 'castle':
      default:
        return this.createCastleFortress();
    }
  }

  static createCastleFortress() {
    const w = 15, h = 10, d = 15;
    const blocks = [];
    const palette = {
      0: { id: 0, name: 'minecraft:stone_bricks' },
      1: { id: 1, name: 'minecraft:cobblestone' },
      2: { id: 2, name: 'minecraft:oak_planks' },
      3: { id: 3, name: 'minecraft:oak_log' },
      4: { id: 4, name: 'minecraft:glass' },
      5: { id: 5, name: 'minecraft:water' }
    };

    for (let y = 0; y < h; y++) {
      for (let z = 0; z < d; z++) {
        for (let x = 0; x < w; x++) {
          let p = -1;

          // Moat water around perimeter at y=0
          if (y === 0) {
            if (x === 0 || x === w - 1 || z === 0 || z === d - 1) p = 5;
            else p = 0; // stone brick floor
          } else if (y <= 5) {
            // Castle walls
            const isBorder = (x === 1 || x === w - 2 || z === 1 || z === d - 2);
            if (isBorder) {
              // Towers at corners
              const isCorner = (x === 1 || x === w - 2) && (z === 1 || z === d - 2);
              if (isCorner) p = 3; // oak logs
              else if (y === 3 && (x === 7 || z === 7)) p = 4; // glass window slots
              else p = 0;
            } else if (y === 1) {
              p = 2; // interior oak flooring
            }
          } else if (y === 6) {
            // Battlements (crenelations)
            const isBorder = (x === 1 || x === w - 2 || z === 1 || z === d - 2);
            if (isBorder && (x + z) % 2 === 0) p = 1;
          }

          if (p !== -1) {
            blocks.push({ x, y, z, p, id: palette[p].name });
          }
        }
      }
    }

    return {
      metadata: {
        version: '26.2',
        bounds: { min: [0, 0, 0], max: [w - 1, h - 1, d - 1] },
        dimensions: { x: w, y: h, z: d }
      },
      palette,
      blocks
    };
  }

  static createMedievalHouse() {
    const w = 11, h = 8, d = 11;
    const blocks = [];
    const palette = {
      0: { id: 0, name: 'minecraft:cobblestone' },
      1: { id: 1, name: 'minecraft:oak_log' },
      2: { id: 2, name: 'minecraft:oak_planks' },
      3: { id: 3, name: 'minecraft:glass' },
      4: { id: 4, name: 'minecraft:bricks' }
    };

    for (let y = 0; y < h; y++) {
      for (let z = 0; z < d; z++) {
        for (let x = 0; x < w; x++) {
          let p = -1;

          if (y === 0) p = 0; // Cobblestone foundation
          else if (y <= 3) {
            // Pillars at corners
            if ((x === 1 || x === w - 2) && (z === 1 || z === d - 2)) p = 1;
            else if (x === 1 || x === w - 2 || z === 1 || z === d - 2) {
              if (y === 2 && (x === 5 || z === 5)) p = 3; // Glass windows
              else p = 2; // Oak plank walls
            }
          } else if (y >= 4 && y < 7) {
            // Stepped roof
            const step = y - 3;
            if (x >= step && x < w - step && z >= step && z < d - step) {
              if (x === step || x === w - step - 1 || z === step || z === d - step - 1) {
                p = 4; // Brick roof
              }
            }
          }

          if (p !== -1) {
            blocks.push({ x, y, z, p, id: palette[p].name });
          }
        }
      }
    }

    return {
      metadata: {
        version: '26.2',
        bounds: { min: [0, 0, 0], max: [w - 1, h - 1, d - 1] },
        dimensions: { x: w, y: h, z: d }
      },
      palette,
      blocks
    };
  }

  static createUndergroundMine() {
    const w = 12, h = 9, d = 14;
    const blocks = [];
    const palette = {
      0: { id: 0, name: 'minecraft:stone' },
      1: { id: 1, name: 'minecraft:oak_log' },
      2: { id: 2, name: 'minecraft:oak_planks' },
      3: { id: 3, name: 'minecraft:diamond_block' },
      4: { id: 4, name: 'minecraft:gold_block' }
    };

    for (let y = 0; y < h; y++) {
      for (let z = 0; z < d; z++) {
        for (let x = 0; x < w; x++) {
          let p = -1;

          // Solid stone cavern walls around perimeter
          if (x === 0 || x === w - 1 || y === 0 || y === h - 1) {
            p = 0;
            // Place rich mineral ores randomly
            if ((x * 13 + y * 7 + z * 19) % 17 === 0) p = 3; // diamond
            else if ((x * 11 + y * 5 + z * 23) % 11 === 0) p = 4; // gold
          } else if (x === 3 || x === w - 4) {
            // Wooden mine shafts support beams
            if (z % 4 === 2 && y <= 5) p = 1;
          } else if (y === 5 && z % 4 === 2 && x > 3 && x < w - 4) {
            p = 2; // cross plank supports
          }

          if (p !== -1) {
            blocks.push({ x, y, z, p, id: palette[p].name });
          }
        }
      }
    }

    return {
      metadata: {
        version: '26.2',
        bounds: { min: [0, 0, 0], max: [w - 1, h - 1, d - 1] },
        dimensions: { x: w, y: h, z: d }
      },
      palette,
      blocks
    };
  }
}
