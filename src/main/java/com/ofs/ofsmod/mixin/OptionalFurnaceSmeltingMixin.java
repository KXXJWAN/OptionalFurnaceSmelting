package com.ofs.ofsmod.mixin;

import com.ofs.ofsmod.network.OptionalFurnaceSmeltingPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(AbstractFurnaceScreen.class)
public class OptionalFurnaceSmeltingMixin {

    @Unique
    private AbstractContainerScreen<?> ofsmod$asContainerScreen() {
        // Eliminate the warnings of ClassCastException
        return (AbstractContainerScreen<?>) (Object) this;
    }

    @Unique
    private List<RecipeHolder<SmeltingRecipe>> ofsmod$fetchRecipeHolders(ItemStack inputStack) {
        // When inputStack was Empty
        if (inputStack.isEmpty()) return Collections.emptyList();
        // When world was not loaded
        Level level = Minecraft.getInstance().level;
        if (level == null) return Collections.emptyList();
        // Fetch all recipe holders
        SingleRecipeInput input = new SingleRecipeInput(inputStack);
        return level.getRecipeManager()
                .getRecipesFor(RecipeType.SMELTING, input, level);
    }

    @Unique
    private int ofsmod$pageIndex = 0;
    @Unique
    private int ofsmod$selectedIndex = -1;
    @Unique
    private static final int ofsmod$GUI_WIDTH = 176;
    @Unique
    private static final int ofsmod$GUI_HEIGHT = 166;
    @Unique
    private static final int ofsmod$SLOTS_PER_PAGE = 5;
    @Unique
    private static final int ofsmod$SLOT_SIZE = 18;
    @Unique
    private static final int ofsmod$MENU_WIDTH = 16;
    @Unique
    private static final int ofsmod$BUTTON_H = 12;
    @Unique
    private static final int ofsmod$MARGIN = 8;
    @Unique
    private ItemStack ofsmod$hoveredResult = ItemStack.EMPTY;

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void ofsmod$renderSideMenu(GuiGraphics guiGraphics, float partialTick,
                                       int mouseX, int mouseY, CallbackInfo ci) {
        ofsmod$hoveredResult = ItemStack.EMPTY;
        AbstractContainerScreen<?> screen = this.ofsmod$asContainerScreen();
        AbstractFurnaceMenu menu = (AbstractFurnaceMenu) screen.getMenu();
        // get input slot of furnace
        ItemStack input = menu.getSlot(0).getItem();
        // get all recipes of input item
        List<ItemStack> results = ofsmod$fetchResults(input);
        if (results.size() == 1) {
            var holders = ofsmod$fetchRecipeHolders(input);
            PacketDistributor.sendToServer(new OptionalFurnaceSmeltingPayload(holders.getFirst().id()));
            return;
        } else if (results.isEmpty()) {
            return;
        }
        // get X-axis coordinate of furnace's GUI
        int leftPos = screen.getGuiLeft();
        // get Y-axis coordinate of furnace's GUI
        int topPos = screen.getGuiTop();
        // calculate total pages
        int maxPage = Math.max(0, (results.size() - 1) / ofsmod$SLOTS_PER_PAGE);
        if (ofsmod$pageIndex > maxPage) {
            ofsmod$pageIndex = maxPage;
        }

        boolean hasPrev = ofsmod$pageIndex > 0;
        boolean hasNext = (ofsmod$pageIndex + 1) * ofsmod$SLOTS_PER_PAGE < results.size();

        // internal height of menu
        int contentH = ofsmod$BUTTON_H + 2
                + (ofsmod$SLOTS_PER_PAGE * ofsmod$SLOT_SIZE) + 2
                + ofsmod$BUTTON_H;
        // get X-axis coordinate of menu
        int menuX = leftPos + ofsmod$GUI_WIDTH + ofsmod$MARGIN;
        // get Y-axis coordinate of menu
        int menuY = topPos + (ofsmod$GUI_HEIGHT - contentH) / 2;
        // get Y-axis coordinate of slot
        int slotBaseY = menuY + ofsmod$BUTTON_H + 2;

        int startIdx = ofsmod$pageIndex * ofsmod$SLOTS_PER_PAGE;

        // rendering of menu
        // rendering of outline
        guiGraphics.fill(menuX - 2, menuY - 2,
                menuX + ofsmod$MENU_WIDTH + 2, menuY + contentH + 2, 0xFF222222);
        // rendering out inline
        guiGraphics.fill(menuX - 1, menuY - 1,
                menuX + ofsmod$MENU_WIDTH + 1, menuY + contentH + 1, 0xFF444444);

        // previous page button rendering
        int prevY = menuY;
        int prevColor = hasPrev ? 0xFF888888 : 0xFF555555;
        guiGraphics.fill(menuX, prevY,
                menuX + ofsmod$MENU_WIDTH, prevY + ofsmod$BUTTON_H, prevColor);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, "▲",
                menuX + ofsmod$MENU_WIDTH / 2, prevY + 2,
                hasPrev ? 0xFFFFFFFF : 0xFFAAAAAA);

        // rendering of five slots
        for (int i = 0; i < ofsmod$SLOTS_PER_PAGE; i++) {
            int idx = startIdx + i;
            int slotY = slotBaseY + i * ofsmod$SLOT_SIZE;

            // base color of slots
            guiGraphics.fill(menuX, slotY, menuX + 16, slotY + 16, 0xFF8B8B8B);
            guiGraphics.fill(menuX, slotY, menuX + 16, slotY + 1, 0xFFFFFFFF);
            guiGraphics.fill(menuX, slotY, menuX + 1, slotY + 16, 0xFFFFFFFF);
            guiGraphics.fill(menuX + 15, slotY, menuX + 16, slotY + 16, 0xFF555555);
            guiGraphics.fill(menuX, slotY + 15, menuX + 16, slotY + 16, 0xFF555555);


            // tooltip rendering
            if (idx < results.size()) {
                ItemStack stack = results.get(idx);
                guiGraphics.renderItem(stack, menuX, slotY);
                guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, menuX, slotY);
                if (ofsmod$inRect(mouseX, mouseY, menuX, slotY, 16, 16)) {
                    ofsmod$hoveredResult = stack;
                }
            }

            // rendering of highlights
            if (idx == ofsmod$selectedIndex) {
                guiGraphics.fill(menuX - 1, slotY - 1, menuX + 17, slotY, 0xFF00FF00);
                guiGraphics.fill(menuX - 1, slotY + 16, menuX + 17, slotY + 17, 0xFF00FF00);
                guiGraphics.fill(menuX - 1, slotY, menuX, slotY + 16, 0xFF00FF00);
                guiGraphics.fill(menuX + 16, slotY, menuX + 17, slotY + 16, 0xFF00FF00);
            }
        }

        // next page rendering
        int nextY = slotBaseY + ofsmod$SLOTS_PER_PAGE * ofsmod$SLOT_SIZE + 2;
        int nextColor = hasNext ? 0xFF888888 : 0xFF555555;
        guiGraphics.fill(menuX, nextY,
                menuX + ofsmod$MENU_WIDTH, nextY + ofsmod$BUTTON_H, nextColor);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, "▼",
                menuX + ofsmod$MENU_WIDTH / 2, nextY + 2,
                hasNext ? 0xFFFFFFFF : 0xFFAAAAAA);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void ofsmod$onClick(double mouseX, double mouseY, int button,
                                CallbackInfoReturnable<Boolean> cir) {
        if (button != 0) {
            return;
        }

        AbstractContainerScreen<?> screen = this.ofsmod$asContainerScreen();

        int leftPos = screen.getGuiLeft();
        int topPos = screen.getGuiTop();
        int contentH = ofsmod$BUTTON_H + 2
                + (ofsmod$SLOTS_PER_PAGE * ofsmod$SLOT_SIZE) + 2
                + ofsmod$BUTTON_H;
        int menuX = leftPos + ofsmod$GUI_WIDTH + ofsmod$MARGIN;
        int menuY = topPos + (ofsmod$GUI_HEIGHT - contentH) / 2;

        AbstractFurnaceMenu menu = (AbstractFurnaceMenu) screen.getMenu();
        List<ItemStack> results = ofsmod$fetchResults(menu.getSlot(0).getItem());
        if (results.size() < 2) return;
        int maxPage = Math.max(0, (results.size() - 1) / ofsmod$SLOTS_PER_PAGE);
        if (ofsmod$pageIndex > maxPage) {
            ofsmod$pageIndex = maxPage;
        }

        int slotBaseY = menuY + ofsmod$BUTTON_H + 2;
        int startIdx = ofsmod$pageIndex * ofsmod$SLOTS_PER_PAGE;

        boolean hasPrev = ofsmod$pageIndex > 0;
        boolean hasNext = (ofsmod$pageIndex + 1) * ofsmod$SLOTS_PER_PAGE < results.size();

        // mouseclicked detection of previous page button
        if (hasPrev && ofsmod$inRect(mouseX, mouseY, menuX, menuY,
                ofsmod$MENU_WIDTH, ofsmod$BUTTON_H)) {
            ofsmod$pageIndex--;
            ofsmod$playClick();
            cir.setReturnValue(true);
            return;
        }

        // mouseclicked detection of next page button
        int nextY = slotBaseY + ofsmod$SLOTS_PER_PAGE * ofsmod$SLOT_SIZE + 2;
        if (hasNext && ofsmod$inRect(mouseX, mouseY, menuX, nextY,
                ofsmod$MENU_WIDTH, ofsmod$BUTTON_H)) {
            ofsmod$pageIndex++;
            ofsmod$playClick();
            cir.setReturnValue(true);
            return;
        }

        // ensure the result when clicked
        for (int i = 0; i < ofsmod$SLOTS_PER_PAGE; i++) {
            int globalIdx = startIdx + i;
            int slotY = slotBaseY + i * ofsmod$SLOT_SIZE;

            if (ofsmod$inRect(mouseX, mouseY, menuX, slotY, 16, 16)) {
                ItemStack input = menu.getSlot(0).getItem();
                var recipes = ofsmod$fetchRecipeHolders(input);
                if (globalIdx >= 0 && globalIdx < results.size()) {
                    RecipeHolder<SmeltingRecipe> holder = recipes.get(globalIdx);
                    ResourceLocation id = holder.id();
                    ofsmod$selectedIndex = globalIdx;
                    ofsmod$playClick();
                    PacketDistributor.sendToServer(new OptionalFurnaceSmeltingPayload(id));
                }
                cir.setReturnValue(true);
                return;
            }
        }
    }

    // adding Tooltip
    @Inject(method = "render", at = @At("TAIL"))
    private void ofsmod$renderCustomTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY,
                                            float partialTick, CallbackInfo ci) {
        if (!ofsmod$hoveredResult.isEmpty()) {
            guiGraphics.renderTooltip(
                    Minecraft.getInstance().font,
                    ofsmod$hoveredResult,
                    mouseX,
                    mouseY
            );
        }
    }

    @Unique
    private List<ItemStack> ofsmod$fetchResults(ItemStack inputStack) {
        if (inputStack.isEmpty()) {
            return Collections.emptyList();
        }
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return Collections.emptyList();
        }

        SingleRecipeInput input = new SingleRecipeInput(inputStack);
        List<RecipeHolder<SmeltingRecipe>> recipes = level.getRecipeManager()
                .getRecipesFor(RecipeType.SMELTING, input, level);

        return recipes.stream()
                .map((RecipeHolder<SmeltingRecipe> h) -> h.value().assemble(input, level.registryAccess()))
                .toList();
    }

    @Unique
    private boolean ofsmod$inRect(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Unique
    private void ofsmod$playClick() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}