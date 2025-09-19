package com.nekoschema.mobconfinement;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.nekoschema.mobconfinement.item.ModItems;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy {

    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        // Configファイルの読み込み 現時点では意味なし
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        ModItems.preInit();
    }

    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        MobConfinement.LOG.info("I am MobConfinement at version " + Tags.VERSION);

        // 本来のレシピを登録
        GameRegistry.addShapedRecipe(
            new ItemStack(ModItems.mob_confinement),
            " X ",
            "XYX",
            " X ",
            'X',
            Items.egg, // 'X'は卵
            'Y',
            Items.redstone // 'Y'はレッドストーン
        );
    }

    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {}

    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {}
}
