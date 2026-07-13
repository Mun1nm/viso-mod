package com.visomod;

import com.visomod.command.ExportCommand;
import com.visomod.item.ExportWandItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisoMod implements ModInitializer {
    public static final String MOD_ID = "visomod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // In Minecraft 26.2, net.minecraft.resources.Identifier is the correct class
    public static final Identifier EXPORT_WAND_ID = Identifier.fromNamespaceAndPath(MOD_ID, "export_wand");

    public static final ResourceKey<Item> EXPORT_WAND_KEY = ResourceKey.create(
            Registries.ITEM,
            EXPORT_WAND_ID
    );

    public static final ExportWandItem EXPORT_WAND = new ExportWandItem(
            new Item.Properties().setId(EXPORT_WAND_KEY)
    );

    @Override
    public void onInitialize() {
        LOGGER.info("[VisoMod] Inicializando Mine-to-Web Isometric Exporter para Minecraft 26.2...");

        // Register Export Wand Item
        Registry.register(BuiltInRegistries.ITEM, EXPORT_WAND_KEY, EXPORT_WAND);

        // Add to Tools Creative Tab
        // In 26.2 Fabric API, ItemGroupEvents was renamed to CreativeModeTabEvents with modifyOutputEvent
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(entries -> entries.prepend(EXPORT_WAND));

        // Register left-click selection handler (AttackBlockCallback still works in 26.2)
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
                ExportWandItem.onAttackBlock(player, hand, pos)
        );

        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                com.visomod.command.ExportCommand.register(dispatcher)
        );

        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            com.visomod.selection.SelectionManager.getInstance().clearSelection();
        });

        LOGGER.info("[VisoMod] Itens e comandos registrados com sucesso!");
    }
}
