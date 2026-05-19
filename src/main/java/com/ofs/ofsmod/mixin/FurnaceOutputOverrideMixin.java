package com.ofs.ofsmod.mixin;

import com.ofs.ofsmod.OptionalFurnaceSmelting;
import com.ofs.ofsmod.attachment.ModAttachments;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import javax.annotation.Nullable;

@Mixin(AbstractFurnaceBlockEntity.class)
public class FurnaceOutputOverrideMixin {

    //Modify result of 'canBurn' method
    @ModifyVariable(
            method = "canBurn",
            at = @At("STORE"),
            ordinal = 0
    )
    private static ItemStack ofsmod$modifyCanBurnOutput(
            ItemStack original,
            RegistryAccess registryAccess,
            @Nullable RecipeHolder<?> recipe,
            NonNullList<ItemStack> inventory,
            int maxStackSize,
            AbstractFurnaceBlockEntity furnace
    ) {
        return ofsmod$getTargetOrOriginal(furnace, original);
    }

    //Modify result of 'burn' method
    @ModifyVariable(
            method = "burn",
            at = @At("STORE"),
            ordinal = 1
    )
    private static ItemStack ofsmod$modifyBurnOutput(
            ItemStack original,
            RegistryAccess registryAccess,
            @Nullable RecipeHolder<?> recipe,
            NonNullList<ItemStack> inventory,
            int maxStackSize,
            AbstractFurnaceBlockEntity furnace
    ) {
        return ofsmod$getTargetOrOriginal(furnace, original);
    }

    @Unique
    private static ItemStack ofsmod$getTargetOrOriginal(AbstractFurnaceBlockEntity furnace, ItemStack original) {
        ItemStack target = furnace.getData(ModAttachments.SMELT_TARGET.get());
        if (target.isEmpty()) return original; // When selected result was not changed

        // When selected result was changed
        ItemStack replacement = target.copy();
        replacement.setCount(original.getCount());
        return replacement;
    }
}