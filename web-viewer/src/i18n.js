export const translations = {
  en: {
    "title": "Viso Web Viewer",
    "drop_file": "Drop your JSON/GZ file here",
    "select_file": "Select File",
    "layer_slice": "Y-Layer Slice",
    "layer_up": "Up",
    "layer_down": "Down",
    "culling": "Hide Interior (Culling)",
    "single_layer": "Single Y-Layer Only",
    "distinct_colors": "Distinct Colors (Legend)",
    "shadow_style": "Y-1 Shadow Style:",
    "shadow_opacity": "1. Opacity Only",
    "shadow_wireframe": "2. Wireframe Mode",
    "shadow_shrink": "3. Shrink Scale",
    "shadow_tint": "4. Depth Tint (Dark Blue)",
    "legend_title": "Block Legend",
    "view_3d": "3D",
    "view_2d": "2D",
    "angle_ne": "NE 45°",
    "angle_se": "SE 135°",
    "angle_sw": "SW 225°",
    "angle_nw": "NW 315°",
    "angle_n": "N 0°",
    "angle_e": "E 90°",
    "angle_s": "S 180°",
    "angle_w": "W 270°",
    "btn_reset": "Reset",
    "btn_grid": "Grid",
    "btn_shadows": "Shadows",
    "inspector_solid": "Solid Block",
    "inspector_transparent": "Transparent Block",
    "controls": "Controls",
    "layers": "Layers",
    "legend": "Legend",
    "view_group": "View",
    "angle_group": "Angle",
    "camera_group": "Camera",
    "env_group": "Environment", "distinct_colors_hint": "Enable 'Distinct Colors' in the Layers menu to view the legend."
  },
  pt: {
    "title": "Viso Web Viewer",
    "drop_file": "Solte o arquivo JSON/GZ aqui",
    "select_file": "Selecionar Arquivo",
    "layer_slice": "Fatia Y (Slice)",
    "layer_up": "Sobe",
    "layer_down": "Desce",
    "culling": "Ocultar Interior (Culling)",
    "single_layer": "Apenas Camada Y Selecionada",
    "distinct_colors": "Cores Distintas (Legenda)",
    "shadow_style": "Estilo da Sombra Y-1:",
    "shadow_opacity": "1. Apenas Opacidade",
    "shadow_wireframe": "2. Modo Wireframe (Aramado)",
    "shadow_shrink": "3. Escala Reduzida (Achatado)",
    "shadow_tint": "4. Filtro de Mergulho (Tint Azul Escuro)",
    "legend_title": "Legenda de Blocos",
    "view_3d": "3D",
    "view_2d": "2D",
    "angle_ne": "NE 45°",
    "angle_se": "SE 135°",
    "angle_sw": "SW 225°",
    "angle_nw": "NW 315°",
    "angle_n": "N 0°",
    "angle_e": "L 90°",
    "angle_s": "S 180°",
    "angle_w": "O 270°",
    "btn_reset": "Resetar",
    "btn_grid": "Grade",
    "btn_shadows": "Sombras",
    "inspector_solid": "Bloco Sólido",
    "inspector_transparent": "Bloco Transparente",
    "controls": "Controles",
    "layers": "Camadas",
    "legend": "Legenda",
    "view_group": "Visão",
    "angle_group": "Ângulo",
    "camera_group": "Câmera",
    "env_group": "Ambiente", "distinct_colors_hint": "Ative as 'Cores Distintas' no menu de Camadas para visualizar a legenda."
  }
};

let currentLang = localStorage.getItem('viso-lang') || 'en';

export function setLanguage(lang) {
  if (translations[lang]) {
    currentLang = lang;
    localStorage.setItem('viso-lang', lang);
    applyTranslations();
  }
}

export function getLanguage() {
  return currentLang;
}

export function t(key) {
  return translations[currentLang][key] || key;
}

export function applyTranslations() {
  document.querySelectorAll('[data-i18n]').forEach(el => {
    const key = el.getAttribute('data-i18n');
    if (translations[currentLang][key]) {
      if (el.tagName === 'INPUT' && el.type === 'button') {
        el.value = translations[currentLang][key];
      } else {
        el.textContent = translations[currentLang][key];
      }
    }
  });
}
