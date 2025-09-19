package com.nekoschema.mobconfinement.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public class ItemMobConfinement extends Item {

    public ItemMobConfinement(String name, String texture) {
        this.setUnlocalizedName(name);
        this.setTextureName(texture);
        this.setCreativeTab(CreativeTabs.tabMisc);
        this.setMaxStackSize(1);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        // 座標計算
        if (side == 0) y--;
        if (side == 1) y++;
        if (side == 2) z--;
        if (side == 3) z++;
        if (side == 4) x--;
        if (side == 5) x++;
        double advance = 0.0D;

        if (spawnEntity(stack, player, world, (double) x + 0.5D, (double) y + advance, (double) z + 0.5D)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean spawnEntity(ItemStack itemStack, EntityPlayer player, World world, double x, double y, double z) {
        itemStack.setItemDamage(0);

        if (!itemStack.hasTagCompound()) {
            return false;
        } else {
            NBTTagCompound nbttagcompound = itemStack.getTagCompound();
            NBTTagCompound entityNBT = nbttagcompound.getCompoundTag("Mob");

            if (entityNBT == null) {
                return false;
            } else {
                Entity entity = EntityList.createEntityFromNBT(entityNBT, world);

                if (entity instanceof EntityLiving) {
                    entity.setLocationAndAngles(x, y, z, world.rand.nextFloat() * 360.0F, 0.0F);
                    ((EntityLiving) entity).rotationYawHead = ((EntityLiving) entity).rotationYaw;
                    ((EntityLiving) entity).renderYawOffset = ((EntityLiving) entity).rotationYaw;

                    if (!world.isRemote) {
                        world.spawnEntityInWorld(entity);
                    }

                    // 名前をデフォルトに戻す
                    ItemStack a = new ItemStack(itemStack.getItem(), 1, 0);
                    itemStack.setStackDisplayName(a.getDisplayName());
                    onSpawned(itemStack);
                    return true;
                } else {
                    onSpawned(itemStack);
                    return false;
                }
            }
        }
    }

    protected void onSpawned(ItemStack stack) {
        // タグリセット
        stack.setTagCompound(null);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity target) {
        if (player.worldObj.isRemote) {
            return false;
        }

        // 既にMobが捕獲されている場合は捕獲不可
        if (stack.hasTagCompound() && stack.getTagCompound()
            .hasKey("Mob")) {
            return false;
        }

        // プレイヤーは捕獲不可
        if (target instanceof EntityPlayer) {
            return false;
        }

        // EntityLivingのみ捕獲可能
        if (target instanceof EntityLiving) {
            EntityLiving living = (EntityLiving) target;

            // 死亡したMobは捕獲不可
            if (living.isDead) {
                return false;
            }

            // 空のアイテムから捕獲済みアイテムを作成
            ItemStack capturedStack = new ItemStack(this, 1, 1);
            capturedStack.setItemDamage(1);
            capturedStack.setTagCompound(new NBTTagCompound());

            NBTTagCompound nbttagcompound = capturedStack.getTagCompound();
            NBTTagCompound entityNBT = new NBTTagCompound();
            living.writeToNBT(entityNBT);

            // Mob IDを設定
            String entityName = (String) EntityList.classToStringMapping.get(living.getClass());
            if (entityName != null) {
                entityNBT.setString("id", entityName);
            }

            // リセットすべき値を設定
            entityNBT.setShort("HurtTime", (short) 0);
            entityNBT.setTag("Motion", this.newDoubleNBTList(new double[] { 0.0D, 0.0D, 0.0D }));
            entityNBT.setFloat("FallDistance", 0.0F);

            nbttagcompound.setTag("Mob", entityNBT);

            // カスタム名があれば保存 (1.7.10対応)
            String customName = entityNBT.hasKey("CustomName") ? entityNBT.getString("CustomName") : entityName;
            if (customName != null && !customName.equals("")) {
                nbttagcompound.setString("NameTag", customName);
                capturedStack.setStackDisplayName("Confinement:" + customName);
            } else {
                nbttagcompound.setString("NameTag", "Unknown");
            }

            // 元のアイテムを減らして新しいアイテムを追加
            stack.stackSize--;

            if (stack.stackSize <= 0) {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, capturedStack);
            } else if (!player.inventory.addItemStackToInventory(capturedStack)) {
                player.entityDropItem(capturedStack, 1);
            }

            // Mobを削除
            living.setDead();
            return true;
        }

        return false;
    }

    protected NBTTagList newDoubleNBTList(double[] arrayOfDouble) {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < arrayOfDouble.length; ++i) {
            nbttaglist.appendTag(new NBTTagDouble(arrayOfDouble[i]));
        }

        return nbttaglist;
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        // Mobが捕獲されている場合はエンチャント効果を表示
        return stack.hasTagCompound() && stack.getTagCompound()
            .hasKey("Mob");
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        if (stack.getTagCompound() != null && stack.getTagCompound()
            .hasKey("Mob")) {
            NBTTagCompound mobData = stack.getTagCompound()
                .getCompoundTag("Mob");
            String mobId = mobData.getString("id");
            list.add("Captured: " + mobId);

            // カスタム名があれば表示
            String nameTag = stack.getTagCompound()
                .getString("NameTag");
            if (nameTag != null && !nameTag.equals("") && !nameTag.equals("Unknown")) {
                list.add("Name: " + nameTag);
            }
        } else {
            list.add("Empty");
        }
    }
}
