package com.ofs.ofsmod;

import com.ofs.ofsmod.attachment.ModAttachments;
import com.ofs.ofsmod.network.OptionalFurnaceSmeltingPayload;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(OptionalFurnaceSmelting.MODID)
public class OptionalFurnaceSmelting {
    public static final String MODID = "optionalfurnacesmelting";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OptionalFurnaceSmelting(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        // 注册网络包
        modEventBus.register(this.getClass());

        // 注册配置文件
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // 注册数据附件
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Optional Furnace Smelting has been loaded");
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(
                        OptionalFurnaceSmeltingPayload.TYPE,
                        OptionalFurnaceSmeltingPayload.STREAM_CODEC,
                        OptionalFurnaceSmeltingPayload::handle
                );
    }
}



