package com.ofs.ofsmod.network;

import com.ofs.ofsmod.attachment.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

    public static void handle(OptionalFurnaceSmeltingPayload payload, IPayloadContext context) {
        // When player was on the server
        if (!(context.player() instanceof ServerPlayer player)) return;
        // When player was opening the furnace
        if (!(player.containerMenu instanceof AbstractFurnaceMenu menu)) return;
        // Get the input slot
        ItemStack input = menu.getSlot(0).getItem();
        // When input slot was not empty
        if (input.isEmpty()) return;
        // Get player's level
        Level level = player.level();
        // Get recipes of current input item
        var recipes = level.getRecipeManager()
                .getRecipesFor(RecipeType.SMELTING, new SingleRecipeInput(input), level);
        // Use ResourceLocation to match precisely
        ItemStack target = null;
        for (var holder : recipes) {
            if (holder.id().equals(payload.recipeId())) {
                target = holder.value().assemble(
                        new SingleRecipeInput(input),
                        level.registryAccess());
                break;
            }
        }
        if (target == null) return; // When recipe was not found
        // Update the attachments data
        Container container = menu.getSlot(0).container;
        if (container instanceof AbstractFurnaceBlockEntity furnace) {
            furnace.setData(ModAttachments.SMELT_TARGET.get(), target);
        }
    }
}