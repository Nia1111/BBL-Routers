package com.benbenlaw.routers.screen;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.item.FilterItem;
import com.benbenlaw.routers.item.FilterType;
import com.benbenlaw.routers.item.RoutersDataComponents;
import com.benbenlaw.routers.item.RoutersItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_TAB;

public class ConfigScreen extends Screen {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Routers.MOD_ID, "textures/gui/config_gui.png");

    private final int imageWidth = 176;
    private final int imageHeight = 72;

    private final ItemStack stack;

    private EditBox searchBox;
    private EditBox countBox;
    private final List<ItemStack> previewStacks = new ArrayList<>();
    private int previewIndex = 0;
    private long lastSwitchTime = 0;
    private static final long SWITCH_INTERVAL = 1000;

    private List<String> suggestions = new ArrayList<>();
    private int selectedSuggestion = -1;
    private boolean suppressSuggestions = false;
    public ConfigScreen(Component title, ItemStack stack) {
        super(title);
        this.stack = stack;
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        addRenderableWidget(Button.builder(Component.literal("✓"), b -> {
            applySelected();this.onClose();
            }).bounds(x + 152, y + 48, 20, 20).build()
        );

        searchBox = new EditBox(this.font, x + 8, y + 16, 160, 18, Component.literal(""));

        searchBox.setMaxLength(64);
        searchBox.setFocused(true);

        Component searchTextOverlay = Component.literal("");
        if (stack.getItem() instanceof FilterItem filterItem) {
            FilterType filterType = filterItem.filterType;

            switch (filterType) {
                case MOD -> searchTextOverlay = Component.translatable("tooltip.routers.mod_filter_tooltip").withStyle(ChatFormatting.GRAY);
                case TAG -> searchTextOverlay = Component.translatable("tooltip.routers.tag_filter_tooltip").withStyle(ChatFormatting.GRAY);
                case STOCK -> searchTextOverlay = Component.translatable("tooltip.routers.stock_filter_tooltip").withStyle(ChatFormatting.GRAY);
                default -> searchTextOverlay = Component.literal("");
            }
        }
        
        searchBox.setTooltip(Tooltip.create(searchTextOverlay));
        searchBox.setResponder(this::updateSuggestions);

        addRenderableWidget(searchBox);

        countBox = new EditBox(this.font, x + 8, y + 36, 35, 18, Component.literal(""));

        countBox.setMaxLength(4);
        countBox.setTooltip(Tooltip.create(Component.translatable("tooltip.routers.amount")));
        addRenderableWidget(countBox);

        updateCountVisibility();
        loadFromItem();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(guiGraphics, mouseX, mouseY, delta);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        if (!previewStacks.isEmpty()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastSwitchTime >= SWITCH_INTERVAL) {
                previewIndex = (previewIndex + 1) % previewStacks.size();
                lastSwitchTime = currentTime;
            }

            ItemStack stackToRender = previewStacks.get(previewIndex);
            guiGraphics.item(stackToRender, x + 134, y + 50);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {

        if (!searchBox.isFocused()) {
            return super.keyPressed(event);
        }

        int key = event.key();

        if (key == VK_TAB) {
            String suggestion = getSelectedOrFirst();
            if (suggestion != null) {
                searchBox.setValue(suggestion);
                updateSuggestions(suggestion);
            }
            return true;
        }

        return super.keyPressed(event);
    }

    private void updateSuggestions(String input) {

        if (suppressSuggestions) return;

        suggestions.clear();
        selectedSuggestion = -1;

        if (input.isEmpty()) return;

        if (!(stack.getItem() instanceof FilterItem filter)) return;

        switch (filter.filterType) {
            case MOD -> {
                for (var mod : ModList.get().getMods()) {
                    String id = mod.getModId();
                    if (id.toLowerCase().contains(input.toLowerCase())) {
                        suggestions.add(id);
                    }
                }
            }

            case TAG -> {
                var level = Minecraft.getInstance().level;
                if (level == null) return;

                var registryAccess = level.registryAccess();
                var itemLookup = registryAccess.lookupOrThrow(Registries.ITEM);

                itemLookup.listTagIds().forEach(tagId -> {
                    String value = tagId.location().toString();
                    if (value.toLowerCase().contains(input.toLowerCase())) {
                        suggestions.add(value);
                    }
                });
            }

            case STOCK -> {
                var level = Minecraft.getInstance().level;
                if (level == null) return;

                var registryAccess = level.registryAccess();
                var itemLookup = registryAccess.lookupOrThrow(Registries.ITEM);

                itemLookup.listElements().forEach(holder -> {
                    String id = holder.unwrapKey().get().identifier().toString();
                    if (id.toLowerCase().contains(input.toLowerCase())) {
                        suggestions.add(id);
                    }
                });
            }
        }

        updatePreviewStacks();
    }

    private void applySelected() {

        String value = searchBox.getValue();
        if (value == null || value.isEmpty()) return;

        if (!(stack.getItem() instanceof FilterItem filter)) return;

        switch (filter.filterType) {
            case MOD -> filter.setMod(stack, value);

            case TAG -> {
                Identifier id = Identifier.tryParse(value);
                if (id != null) {
                    filter.setTag(stack, id);
                }
            }

            case STOCK -> {
                Identifier id = Identifier.tryParse(value);
                if (id == null) return;

                var registry = Minecraft.getInstance().level.registryAccess()
                        .lookupOrThrow(Registries.ITEM);

                var holder = registry.get(id).orElse(null);
                if (holder == null) return;

                var raw = countBox.getValue();
                int count;

                try {
                    raw = raw.trim();

                    if (raw.isEmpty()) {
                        var existing = stack.get(RoutersDataComponents.STOCK_FILTER.get());
                        count = (existing != null) ? existing.amount() : 1;
                    } else {
                        count = Integer.parseInt(raw);
                    }

                } catch (NumberFormatException e) {
                    var existing = stack.get(RoutersDataComponents.STOCK_FILTER.get());
                    count = (existing != null) ? existing.amount() : 1;
                }

                filter.setStock(
                        stack,
                        BuiltInRegistries.ITEM.getValue(id).getDefaultInstance(),
                        count
                );
            }
        }

        suggestions.clear();
        selectedSuggestion = -1;
    }

    private String getSelectedOrFirst() {
        if (!suggestions.isEmpty()) {
            if (selectedSuggestion < 0) return suggestions.get(0);
            return suggestions.get(Math.min(selectedSuggestion, suggestions.size() - 1));
        }
        return null;
    }

    private void updateCountVisibility() {
        if (!(stack.getItem() instanceof FilterItem filter)) return;
        countBox.visible = filter.filterType == FilterType.STOCK;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        searchBox.extractRenderState(graphics, mouseX, mouseY, delta);
        countBox.extractRenderState(graphics, mouseX, mouseY, delta);

        graphics.text(Minecraft.getInstance().font, title, x + 8, y + 6, 0xFF555555, false);

        if (!suggestions.isEmpty()) {

            int height = suggestions.size() * 12;

            graphics.fill(searchBox.getX(), searchBox.getY() + 18, searchBox.getX() + 160, searchBox.getY() + 18 + height, 0xAA000000);

            for (int i = 0; i < suggestions.size(); i++) {

                String text = suggestions.get(i);
                int color = (i == selectedSuggestion) ? 0xFFFFAA00 : 0xFFFFFFFF;

                graphics.text(this.font, text, searchBox.getX() + 4, searchBox.getY() + 18 + (i * 12) + 2, color);
            }
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {

        if (!suggestions.isEmpty()) {

            int x = searchBox.getX();
            int y = searchBox.getY() + 18;

            for (int i = 0; i < suggestions.size(); i++) {

                int rowY = y + (i * 12);

                if (event.x() >= x && event.x() <= x + 160 &&
                        event.y() >= rowY && event.y() <= rowY + 12) {

                    searchBox.setValue(suggestions.get(i));
                    applySelected();

                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    private void loadFromItem() {

        if (!(stack.getItem() instanceof FilterItem filter)) return;

        suppressSuggestions = true;

        switch (filter.filterType) {

            case MOD -> {
                var mod = stack.get(RoutersDataComponents.MOD_FILTER.get());
                if (mod != null) {
                    searchBox.setValue(mod);
                }
            }

            case TAG -> {
                var tag = stack.get(RoutersDataComponents.TAG_FILTER.get());
                if (tag != null) {
                    searchBox.setValue(tag.toString());
                }
            }

            case STOCK -> {
                var stockFilter = stack.get(RoutersDataComponents.STOCK_FILTER.get());
                if (stockFilter != null) {
                    countBox.setValue(String.valueOf(stockFilter.amount()));
                    searchBox.setValue(
                            BuiltInRegistries.ITEM.getKey(stockFilter.stack().getItem()).toString()
                    );
                }
            }
        }
        updatePreviewStacks();
        suppressSuggestions = false;
    }

    private void updatePreviewStacks() {
        previewStacks.clear();

        if (!(stack.getItem() instanceof FilterItem filter)) return;

        var level = Minecraft.getInstance().level;
        if (level == null) return;

        var registryAccess = level.registryAccess();
        var itemLookup = registryAccess.lookupOrThrow(Registries.ITEM);

        switch (filter.filterType) {

            case MOD -> {
                String modId = searchBox.getValue();
                if (modId.isEmpty()) return;

                String query = modId.toLowerCase();

                itemLookup.listElements().forEach(holder -> {
                    holder.unwrapKey().ifPresent(key -> {
                        Identifier id = key.identifier();

                        if (id.getNamespace().toLowerCase().contains(query)) {
                            previewStacks.add(new ItemStack(holder.value()));
                        }
                    });
                });
            }

            case TAG -> {
                String tagInput = searchBox.getValue();
                if (tagInput == null || tagInput.isEmpty()) return;

                Identifier tagId = Identifier.tryParse(tagInput);
                if (tagId == null) return;

                var tagKey = TagKey.create(
                        Registries.ITEM,
                        tagId
                );

                itemLookup.get(tagKey).ifPresent(tag -> {
                    for (var holder : tag) {
                        previewStacks.add(new ItemStack(holder.value()));
                    }
                });
            }

            case STOCK -> {
                var stock = stack.get(RoutersDataComponents.STOCK_FILTER.get());
                if (stock != null && !stock.stack().isEmpty()) {
                    previewStacks.add(stock.stack().copy());
                }
            }
        }

        previewIndex = 0;
        lastSwitchTime = System.currentTimeMillis();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}