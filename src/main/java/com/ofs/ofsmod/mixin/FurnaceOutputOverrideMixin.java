package com.ofs.ofsmod.mixin;

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

    /**
     * 修改 canBurn 中的默认产物，使"输出槽兼容性检查"基于玩家选中的目标产物。
     * canBurn 中第一个 ItemStack 局部变量（itemstack）就是默认配方产物，ordinal = 0
     */
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

    /**
     * 修改 burn 中实际放入输出槽的产物。
     * burn 中第二个 ItemStack 局部变量（itemstack1）是 assemble 出来的产物，ordinal = 1
     */
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
        if (target.isEmpty()) {
            return original; // 玩家没选，走原版默认产物
        }
        // 保持原版计算出的数量（通常为 1）
        ItemStack replacement = target.copy();
        replacement.setCount(original.getCount());
        return replacement;
    }
}