import re

with open('/Users/joaoaveraldo/Documents/Minecraft/viso-mod/web-viewer/index.html', 'r') as f:
    content = f.read()

# Insert lang toggle button next to the file upload button
toggle_btn = """
      <div class="lang-toggle">
        <button id="btn-lang-en" class="btn-lang">🇺🇸 EN</button>
        <button id="btn-lang-pt" class="btn-lang">🇧🇷 PT</button>
      </div>
"""
content = content.replace('<label class="btn-upload" for="file-input">', toggle_btn + '\n      <label class="btn-upload" for="file-input">')

# Add data-i18n to text elements
replacements = [
    ('Abrir Exportação (.json / .gz)', '<span data-i18n="select_file">Abrir Exportação (.json / .gz)</span>'),
    ('Solte o arquivo .json.gz ou .json aqui', '<h3 data-i18n="drop_file">Solte o arquivo .json.gz ou .json aqui</h3>'),
    ('<span>Fatia Y (Slice)</span>', '<span data-i18n="layer_slice">Fatia Y (Slice)</span>'),
    ('title="Sobe camada"', 'title="Sobe camada" data-i18n="layer_up"'),
    ('title="Desce camada"', 'title="Desce camada" data-i18n="layer_down"'),
    ('Ocultar Interior (Culling)', '<span data-i18n="culling">Ocultar Interior (Culling)</span>'),
    ('Apenas Camada Y Selecionada', '<span data-i18n="single_layer">Apenas Camada Y Selecionada</span>'),
    ('Cores Distintas (Legenda)', '<span data-i18n="distinct_colors">Cores Distintas (Legenda)</span>'),
    ('Estilo da Sombra Y-1:', '<span data-i18n="shadow_style">Estilo da Sombra Y-1:</span>'),
    ('1. Apenas Opacidade', '1. Apenas Opacidade'), # select options will be handled in JS or via attributes if needed, but let's add them
    ('<option value="opacity">1. Apenas Opacidade</option>', '<option value="opacity" data-i18n="shadow_opacity">1. Apenas Opacidade</option>'),
    ('<option value="wireframe">2. Modo Wireframe (Aramado)</option>', '<option value="wireframe" data-i18n="shadow_wireframe">2. Modo Wireframe (Aramado)</option>'),
    ('<option value="shrink">3. Escala Reduzida (Achatado)</option>', '<option value="shrink" data-i18n="shadow_shrink">3. Escala Reduzida (Achatado)</option>'),
    ('<option value="tint">4. Filtro de Mergulho (Tint Azul Escuro)</option>', '<option value="tint" data-i18n="shadow_tint">4. Filtro de Mergulho (Tint Azul Escuro)</option>'),
    ('<h4>Legenda de Blocos</h4>', '<h4 data-i18n="legend_title">Legenda de Blocos</h4>'),
    ('<span class="view-mode-label">3D</span>', '<span class="view-mode-label" data-i18n="view_3d">3D</span>'),
    ('<span class="view-mode-label">2D</span>', '<span class="view-mode-label" data-i18n="view_2d">2D</span>'),
    ('>NE 45°</button>', ' data-i18n="angle_ne">NE 45°</button>'),
    ('>SE 135°</button>', ' data-i18n="angle_se">SE 135°</button>'),
    ('>SW 225°</button>', ' data-i18n="angle_sw">SW 225°</button>'),
    ('>NW 315°</button>', ' data-i18n="angle_nw">NW 315°</button>'),
    ('>N 0°</button>', ' data-i18n="angle_n">N 0°</button>'),
    ('>L 90°</button>', ' data-i18n="angle_e">L 90°</button>'),
    ('>S 180°</button>', ' data-i18n="angle_s">S 180°</button>'),
    ('>O 270°</button>', ' data-i18n="angle_w">O 270°</button>'),
    ('>Resetar</button>', ' data-i18n="btn_reset">Resetar</button>'),
    ('>Grade</button>', ' data-i18n="btn_grid">Grade</button>'),
    ('>Sombras</button>', ' data-i18n="btn_shadows">Sombras</button>'),
    ('<span>Controles</span>', '<span data-i18n="controls">Controles</span>'),
    ('<span>Camadas</span>', '<span data-i18n="layers">Camadas</span>'),
    ('<span>Legenda</span>', '<span data-i18n="legend">Legenda</span>'),
    ('<span class="control-group-title">Visão</span>', '<span class="control-group-title" data-i18n="view_group">Visão</span>'),
    ('<span class="control-group-title">Ângulo</span>', '<span class="control-group-title" data-i18n="angle_group">Ângulo</span>'),
    ('<span class="control-group-title">Câmera</span>', '<span class="control-group-title" data-i18n="camera_group">Câmera</span>'),
    ('<span class="control-group-title">Ambiente</span>', '<span class="control-group-title" data-i18n="env_group">Ambiente</span>'),
]

for old, new in replacements:
    content = content.replace(old, new)

# Fix double span for select_file
content = content.replace('<span><span data-i18n="select_file">Abrir Exportação (.json / .gz)</span></span>', '<span data-i18n="select_file">Abrir Exportação (.json / .gz)</span>')

# Fix double h3 for drop_file
content = content.replace('<h3><h3 data-i18n="drop_file">Solte o arquivo .json.gz ou .json aqui</h3></h3>', '<h3 data-i18n="drop_file">Solte o arquivo .json.gz ou .json aqui</h3>')

# Fix shadow style label
content = content.replace('<label for="select-shadow-style" class="slicer-label"><span data-i18n="shadow_style">Estilo da Sombra Y-1:</span></label>', '<label for="select-shadow-style" class="slicer-label" data-i18n="shadow_style">Estilo da Sombra Y-1:</label>')
content = content.replace('<label for="select-shadow-style" class="slicer-label">Estilo da Sombra Y-1:</label>', '<label for="select-shadow-style" class="slicer-label" data-i18n="shadow_style">Estilo da Sombra Y-1:</label>')


with open('/Users/joaoaveraldo/Documents/Minecraft/viso-mod/web-viewer/index.html', 'w') as f:
    f.write(content)

