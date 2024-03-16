package com.moujounakki.archimedesfluids.mixins;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BucketItem.class)
public abstract class MixinBucketItem extends Item implements DispensibleContainerItem {
    public MixinBucketItem(Properties p_41383_) {
        super(p_41383_);
    }

    public int getMaxDamage(ItemStack stack) {
        return 8;
    }
    public boolean canBeDepleted() {return true;}
}
