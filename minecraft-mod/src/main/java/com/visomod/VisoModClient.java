package com.visomod;

import com.visomod.render.SelectionRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class VisoModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        VisoMod.LOGGER.info("[VisoMod Client] Inicializando renderizador isométrico de seleção...");
        WorldRenderEvents.LAST.register(new SelectionRenderer());
    }
}
