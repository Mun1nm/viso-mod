import re
with open('/Users/joaoaveraldo/Documents/Minecraft/viso-mod/web-viewer/src/i18n.js', 'r') as f:
    content = f.read()

content = content.replace('"env_group": "Environment"', '"env_group": "Environment", "distinct_colors_hint": "Enable \'Distinct Colors\' in the Layers menu to view the legend."')
content = content.replace('"env_group": "Ambiente"', '"env_group": "Ambiente", "distinct_colors_hint": "Ative as \'Cores Distintas\' no menu de Camadas para visualizar a legenda."')

with open('/Users/joaoaveraldo/Documents/Minecraft/viso-mod/web-viewer/src/i18n.js', 'w') as f:
    f.write(content)
