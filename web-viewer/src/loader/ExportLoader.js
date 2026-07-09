import pako from 'pako';

export class ExportLoader {

  static async loadFromFile(file) {
    const arrayBuffer = await file.arrayBuffer();
    return this.parseBuffer(arrayBuffer);
  }

  static async loadFromUrl(url) {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Arquivo não encontrado (${response.status}) em: ${url}`);
    }
    const arrayBuffer = await response.arrayBuffer();
    return this.parseBuffer(arrayBuffer);
  }

  static async parseBuffer(arrayBuffer) {
    const uint8 = new Uint8Array(arrayBuffer);

    // Verificação precisa dos Magic Bytes do GZIP: 0x1F (31) e 0x8B (139)
    const isActuallyGzip = uint8.length >= 2 && uint8[0] === 0x1f && uint8[1] === 0x8b;

    let jsonString;
    if (isActuallyGzip) {
      try {
        jsonString = pako.ungzip(uint8, { to: 'string' });
      } catch (e) {
        console.error('[ExportLoader] Erro ao descompactar GZIP via pako:', e);
        throw new Error('Falha na descompactação GZIP: ' + (e.message || e));
      }
    } else {
      const decoder = new TextDecoder('utf-8');
      jsonString = decoder.decode(arrayBuffer);
    }

    const rawData = JSON.parse(jsonString);
    return this.normalizeStructure(rawData);
  }

  static normalizeStructure(data) {
    const metadata = data.metadata || {
      version: '26.2',
      bounds: { min: [0, 0, 0], max: [10, 10, 10] },
      dimensions: [10, 10, 10]
    };

    const palette = data.palette || {};
    const blocks = [];

    if (Array.isArray(data.blocks)) {
      for (const item of data.blocks) {
        const x = item.pos ? item.pos.x : item.x;
        const y = item.pos ? item.pos.y : item.y;
        const z = item.pos ? item.pos.z : item.z;

        if (x === undefined || y === undefined || z === undefined) continue;

        let id = item.id;
        if (!id && item.p !== undefined) {
          const pKey = String(item.p);
          const paletteEntry = palette[pKey];
          id = paletteEntry ? paletteEntry.name : 'minecraft:stone';
        }
        if (!id) id = 'minecraft:stone';

        blocks.push({
          x: Number(x),
          y: Number(y),
          z: Number(z),
          p: item.p,
          id
        });
      }
    }

    // Compute dimensions if missing
    let maxX = 0, maxY = 0, maxZ = 0;
    for (const b of blocks) {
      if (b.x > maxX) maxX = b.x;
      if (b.y > maxY) maxY = b.y;
      if (b.z > maxZ) maxZ = b.z;
    }

    const dimensions = metadata.dimensions && Array.isArray(metadata.dimensions) && metadata.dimensions[0] > 0
      ? { x: metadata.dimensions[0], y: metadata.dimensions[1], z: metadata.dimensions[2] }
      : (metadata.dimensions && metadata.dimensions.x !== undefined
          ? metadata.dimensions
          : { x: maxX + 1, y: maxY + 1, z: maxZ + 1 });

    return {
      metadata: {
        ...metadata,
        dimensions
      },
      palette,
      blocks
    };
  }
}
