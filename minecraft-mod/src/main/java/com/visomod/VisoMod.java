package com.visomod;

import com.visomod.command.ExportCommand;
import com.visomod.item.ExportWandItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisoMod implements ModInitializer {
    public static final String MOD_ID = "visomod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Registry key required for item registration in modern Minecraft/Fabric (1.21.2+ / 26.2)
    public static final ResourceKey<Item> EXPORT_WAND_KEY = ResourceKey.create(
            Registries.ITEM, 
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "export_wand")
    );

    public static final ExportWandItem EXPORT_WAND = new ExportWandItem(
            new Item.Properties().setId(EXPORT_WAND_KEY)
    );

    @Override
    public void onInitialize() {
        LOGGER.info("[VisoMod] Inicializando Mine-to-Web Isometric Exporter para Minecraft 26.2...");

        // Register Export Wand Item with its associated ResourceKey
        Registry.register(BuiltInRegistries.ITEM, EXPORT_WAND_KEY, EXPORT_WAND);
        
        // Add to Tools Creative Tab
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(content -> {
            content.accept(EXPORT_WAND);
        });

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
