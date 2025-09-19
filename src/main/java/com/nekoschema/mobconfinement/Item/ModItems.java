package com.nekoschema.mobconfinement.item;

import net.minecraft.item.Item;

import com.nekoschema.mobconfinement.MobConfinement;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModItems {

    public static Item mob_confinement;

    public static void preInit() {
        // MODIDを使用してテクスチャ名を指定
        mob_confinement = new ItemMobConfinement("mob_confinement", MobConfinement.MODID + ":mob_confinement");
        GameRegistry.registerItem(mob_confinement, "mob_confinement");
    }
}
