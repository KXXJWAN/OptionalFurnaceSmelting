package com.ofs.ofsmod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record OptionalFurnaceSmeltingPayload(int globalRecipeIndex,net.minecraft.resources.ResourceLocation recipeId) implements CustomPacketPayload {

    public static final Type<OptionalFurnaceSmeltingPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("ofsmod", "optional_furnace_smelting_target"));

    public static final StreamCodec<ByteBuf, OptionalFurnaceSmeltingPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OptionalFurnaceSmeltingPayload::globalRecipeIndex,ResourceLocation.STREAM_CODEC,
            OptionalFurnaceSmeltingPayload::recipeId,
            OptionalFurnaceSmeltingPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OptionalFurnaceSmeltingPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ServerPayloadHandler.handle(payload, context));
    }
}