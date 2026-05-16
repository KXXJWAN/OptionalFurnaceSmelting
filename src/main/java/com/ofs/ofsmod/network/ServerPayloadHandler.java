package com.ofs.ofsmod.network;

import com.ofs.ofsmod.OptionalFurnaceSmelting;
import com.ofs.ofsmod.attachment.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

    public static void handle(OptionalFurnaceSmeltingPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        if (!(player.containerMenu instanceof AbstractFurnaceMenu menu)) return;

        ItemStack input = menu.getSlot(0).getItem();
        if (input.isEmpty()) return;
        OptionalFurnaceSmelting.LOGGER.info("[OFS] Server received payload: idx={}, recipeId={}",
                payload.globalRecipeIndex(), payload.recipeId());
        var recipes = player.level().getRecipeManager()
                .getRecipesFor(RecipeType.SMELTING, new SingleRecipeInput(input), player.level());

        // 用 ResourceLocation 精确匹配，彻底避免列表顺序不一致问题
        ItemStack target = null;
        for (var holder : recipes) {
            if (holder.id().equals(payload.recipeId())) {
                target = holder.value().assemble(
                        new SingleRecipeInput(input),
                        player.level().registryAccess());
                break;
            }
        }
        if (target == null) return; // 配方未找到，拒绝

        Container container = menu.getSlot(0).container;
        if (container instanceof AbstractFurnaceBlockEntity furnace) {
            furnace.setData(ModAttachments.SMELT_TARGET.get(), target);
        }
    }
}