# Viso — Mine-to-Web Isometric Exporter (Minecraft 26.2)

Ferramenta integrada de exportação 3D de construções do Minecraft (Fabric 26.2) para visualização isométrica interativa e de alta performance no navegador usando Three.js (`InstancedMesh`).

---

## 🚀 Arquitetura do Projeto

O projeto está dividido em dois módulos principais:

```
viso-mod/
├── minecraft-mod/       # Mod para Minecraft 26.2 (Fabric API / Java 21)
└── web-viewer/          # Visualizador Web Isométrico (Three.js + Vite)
```

---

## 🎮 1. Módulo Minecraft (Mod Fabric 26.2)

### Instalação Rápida (Arquivo `.jar` pronto)
O arquivo `.jar` do mod compilado está disponível diretamente na pasta do repositório:
- **`releases/viso-mod-1.0.1.jar`** — Basta copiar para a sua pasta `mods/` do Minecraft Fabric!

### Como compilar manualmente o Mod
Acesse a pasta `minecraft-mod/` e execute:
```bash
cd minecraft-mod
./gradlew build
```
O arquivo `.jar` gerado estará na pasta `minecraft-mod/build/libs/`.

### Como usar no Jogo
1. Equipe a **Varinha de Exportação** (`Export Wand`).
2. **Clique Esquerdo** em um bloco para definir o **Ponto A**.
3. **Clique Direito** em um bloco para definir o **Ponto B**.
4. Uma caixa delimitadora translúcida 3D marcará a região selecionada.
5. Digite o comando no chat:
   ```
   /exportar castelo
   ```
6. O mod varrerá a região, otimizará a paleta de blocos e salvará automaticamente o arquivo comprimido em GZIP:
   - `run/exports/castelo.json.gz` (formato ultra-compacto recomendado para a web)
   - `run/exports/castelo.json` (cópia legível)

---

## 🌐 2. Módulo Web Viewer (Three.js Isometric Viewer)

### Como rodar o Visualizador
Acesse a pasta `web-viewer/` e inicie o servidor de desenvolvimento:
```bash
cd web-viewer
npm install
npm run dev
```
Acesse `http://localhost:3000` no seu navegador.

### Principais Funcionalidades do Visualizador
- **Câmera Isométrica Ortográfica Verdadeira**: Ângulo isométrico de 35.264° com rotação em 4 passos de 90° (NE, SE, SW, NW).
- **InstancedMesh & Interior Culling**: Otimização que agrupa todos os blocos do mesmo tipo em uma única chamada de desenho e oculta blocos 100% internos, garantindo 60 FPS mesmo em estruturas enormes.
- **Controle Dinâmico de Camadas (Y-Slice)**: Explore o interior de casas, minas ou castelos cortando a visualização camada por camada com o slider ou botões `▲` / `▼`.
- **Inspecionador Interativo de Blocos via Raycast**: Passe o mouse sobre qualquer bloco para ver em tempo real o ID, coordenadas `(X, Y, Z)` e propriedades geométricas.
- **Suporte Nativo a GZIP (`.json.gz`)**: Arraste e solte ou abra arquivos `.json.gz` exportados diretamente pelo Minecraft.
