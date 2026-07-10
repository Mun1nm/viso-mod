package com.visomod;

import com.visomod.render.SelectionRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

public class VisoModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        VisoMod.LOGGER.info("[VisoMod Client] Inicializando renderizador isométrico de seleção...");
        // In Fabric API 0.154.2+26.2, WorldRenderEvents.LAST was replaced by LevelRenderEvents.END_MAIN
        LevelRenderEvents.END_MAIN.register(new SelectionRenderer());
    }
}
