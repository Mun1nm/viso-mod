import re

with open('/Users/joaoaveraldo/Documents/Minecraft/viso-mod/web-viewer/src/main.js', 'r') as f:
    content = f.read()

# Add imports
imports = "import { setLanguage, applyTranslations, t } from './i18n.js';\n"
content = imports + content

# Add toggle listeners
init_code = """
    // I18N INIT
    applyTranslations();
    document.getElementById('btn-lang-en')?.addEventListener('click', () => setLanguage('en'));
    document.getElementById('btn-lang-pt')?.addEventListener('click', () => setLanguage('pt'));
"""
content = content.replace('this.initUI();', init_code + '\n    this.initUI();')

# Update inspector translations
content = content.replace("document.getElementById('inspector-block-type').textContent = isTransparent ? 'Bloco Transparente' : 'Bloco Sólido';",
                          "document.getElementById('inspector-block-type').textContent = isTransparent ? t('inspector_transparent') : t('inspector_solid');")
content = content.replace("document.title = `VisoMod — ${structureName}`;",
                          "document.title = `${t('title')} — ${structureName}`;")
content = content.replace("headerName.textContent = structureName;",
                          "headerName.textContent = structureName || t('title');")
content = content.replace("legendList.innerHTML = `<div style=\"color: var(--text-muted); font-size: 13px; text-align: center; padding: 16px 0;\">Ative as \"Cores Distintas\" no menu de Camadas para visualizar a legenda.</div>`;",
                          "legendList.innerHTML = `<div style=\"color: var(--text-muted); font-size: 13px; text-align: center; padding: 16px 0;\">${t('distinct_colors_hint') || 'Ative as \"Cores Distintas\" no menu de Camadas para visualizar a legenda.'}</div>`;")


with open('/Users/joaoaveraldo/Documents/Minecraft/viso-mod/web-viewer/src/main.js', 'w') as f:
    f.write(content)
