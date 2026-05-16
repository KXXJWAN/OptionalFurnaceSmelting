package com.ofs.ofsmod.attachment;

import com.ofs.ofsmod.OptionalFurnaceSmelting;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, OptionalFurnaceSmelting.MODID);

    /** 存储熔炉的“目标产物”；空堆叠表示未指定 */
    public static final Supplier<AttachmentType<ItemStack>> SMELT_TARGET = ATTACHMENT_TYPES.register(
            "smelt_target",
            () -> AttachmentType.builder(() -> ItemStack.EMPTY.copy())
            .serialize(ItemStack.OPTIONAL_CODEC)   // 1.21.1 可用；如报错可换成 ItemStack.CODEC
                    .build()
    );
}