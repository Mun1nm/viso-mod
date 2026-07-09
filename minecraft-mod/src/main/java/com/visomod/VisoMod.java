package com.visomod;

import com.visomod.command.ExportCommand;
import com.visomod.item.ExportWandItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisoMod implements ModInitializer {
    public static final String MOD_ID = "visomod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ExportWandItem EXPORT_WAND = new ExportWandItem(new Item.Settings().maxCount(1));

    @Override
    public void onInitialize() {
        LOGGER.info("[VisoMod] Inicializando Mine-to-Web Isometric Exporter para Minecraft 26.2...");

        // Register Export Wand Item
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "export_wand"), EXPORT_WAND);

        // Register left-click selection handler
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
                ExportWandItem.onAttackBlock(player, pos)
        );

        // Register /exportar command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ExportCommand.register(dispatcher)
        );

        LOGGER.info("[VisoMod] Itens e comandos registrados com sucesso!");
    }
}
