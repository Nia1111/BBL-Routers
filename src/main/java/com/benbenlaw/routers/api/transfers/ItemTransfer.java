package com.benbenlaw.routers.api.transfers;

import com.benbenlaw.core.block.entity.handler.item.FilterItemHandler;
import com.benbenlaw.routers.api.TransferEngine;
import com.benbenlaw.routers.block.custom.RouterBlock;
import com.benbenlaw.routers.block.entity.ExporterBlockEntity;
import com.benbenlaw.routers.block.entity.ImporterBlockEntity;
import com.benbenlaw.routers.item.FilterItem;
import com.benbenlaw.routers.item.FilterType;
import com.benbenlaw.routers.item.RoutersDataComponents;
import com.benbenlaw.routers.util.RoutersTags;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;

public class ItemTransfer {

    public static int transferItems(ServerLevel level, ExporterBlockEntity exporter) {

        var state = level.getBlockState(exporter.getBlockPos());
        Direction facing = state.getValue(RouterBlock.FACING);

        ResourceHandler<ItemResource> source = exporter.getConnectedResources().getItemHandler(
                level, exporter.getBlockPos().relative(facing), facing.getOpposite()
        ).orElse(null);

        if (source == null || source.size() == 0) return exporter.lastImporterIndex;

        return TransferEngine.run(level, exporter, exporter.importerPositions, exporter.isRoundRobin, exporter.lastImporterIndex,
                (srvLevel, entity, targetPos) -> {

                    ResourceHandler<ItemResource> target = getTargetHandler(srvLevel, entity, targetPos);
                    if (target == null) return false;

                    ImporterBlockEntity importer = srvLevel.getBlockEntity(targetPos.pos()) instanceof ImporterBlockEntity imp ? imp : null;
                    ResourceHandler<ItemResource> importerAdjacentHandler = importer != null
                            ? getImporterAdjacentHandler(srvLevel, importer)
                            : null;

                    var moved = ResourceHandlerUtil.moveFirst(source, target, resource -> {
                        boolean isWhitelist = !entity.isBlacklist();

                        if (!ResourceHandlerUtil.isEmpty(entity.getFilterItemHandler())) {
                            if (!checkFilter(entity.getFilterItemHandler(), resource, isWhitelist, entity.isIgnoreNbt())) {
                                return false;
                            }
                        }

                        if (importer != null && !ResourceHandlerUtil.isEmpty(importer.getFilterItemHandler())) {
                            if (!checkImporterFilter(importer.getFilterItemHandler(), resource, isWhitelist, importerAdjacentHandler)) {
                                return false;
                            }
                        }

                        return true;
                    }, entity.getUpgradeValue(RoutersTags.Items.ITEM_UPGRADES), null);

                    return moved != null && moved.amount() > 0;
                });
    }

    private static boolean checkFilter(FilterItemHandler filterHandler, ItemResource resource, boolean isWhitelist, boolean ignoreNbt) {
        ItemStack incoming = resource.toStack();
        boolean hasAnyFilter = false;
        boolean foundMatch = false;

        for (int i = 0; i < filterHandler.size(); i++) {
            ItemStack filterStack = filterHandler.getResource(i).toStack();
            if (filterStack.isEmpty()) continue;

            hasAnyFilter = true;

            if (filterStack.getItem() instanceof FilterItem filterItem) {
                if (filterItem.matches(filterStack, incoming)) {
                    foundMatch = true;
                    break;
                }
            } else {
                boolean matches = ignoreNbt
                        ? resource.getItem() == filterStack.getItem()
                        : ItemStack.isSameItemSameComponents(incoming, filterStack);
                if (matches) {
                    foundMatch = true;
                    break;
                }
            }
        }

        if (!hasAnyFilter) return true;
        return isWhitelist == foundMatch;
    }

    private static boolean checkImporterFilter(FilterItemHandler filterHandler, ItemResource resource, boolean isWhitelist, @Nullable ResourceHandler<ItemResource> adjacentHandler) {
        ItemStack incoming = resource.toStack();
        boolean hasAnyFilter = false;
        boolean foundMatch = false;

        for (int i = 0; i < filterHandler.size(); i++) {
            ItemStack filterStack = filterHandler.getResource(i).toStack();
            if (filterStack.isEmpty()) continue;

            hasAnyFilter = true;

            if (filterStack.getItem() instanceof FilterItem filterItem) {
                if (filterItem.filterType == FilterType.STOCK) {
                    var stock = filterStack.get(RoutersDataComponents.STOCK_FILTER.get());
                    if (stock == null) continue;
                    if (!ItemStack.isSameItemSameComponents(incoming, stock.stack())) continue;

                    int currentCount = countMatchingItems(adjacentHandler, stock.stack());
                    if (currentCount >= stock.amount()) return false;

                    return true;
                }

                if (filterItem.matches(filterStack, incoming)) {
                    foundMatch = true;
                    break;
                }
            } else {
                if (ItemStack.isSameItemSameComponents(incoming, filterStack)) {
                    foundMatch = true;
                    break;
                }
            }
        }

        if (!hasAnyFilter) return true;
        return isWhitelist ? foundMatch : !foundMatch;
    }

    private static int countMatchingItems(@Nullable ResourceHandler<ItemResource> handler, ItemStack target) {
        if (handler == null) return 0;
        int count = 0;
        for (int i = 0; i < handler.size(); i++) {
            ItemResource res = handler.getResource(i);
            if (!res.isEmpty() && ItemStack.isSameItemSameComponents(res.toStack(), target)) {
                count += (int) handler.getAmountAsInt(i);
            }
        }
        return count;
    }

    @Nullable
    private static ResourceHandler<ItemResource> getImporterAdjacentHandler(ServerLevel level, ImporterBlockEntity importer) {
        var state = level.getBlockState(importer.getBlockPos());
        Direction facing = state.getValue(RouterBlock.FACING);
        return level.getCapability(Capabilities.Item.BLOCK, importer.getBlockPos().relative(facing), facing.getOpposite());
    }

    private static ResourceHandler<ItemResource> getTargetHandler(ServerLevel level, ExporterBlockEntity exporter, GlobalPos pos) {
        ServerLevel targetLevel = level.getServer().getLevel(pos.dimension());
        if (targetLevel == null || (!pos.dimension().equals(level.dimension()) && !exporter.canDoDimensionalTravel())) return null;
        if (!targetLevel.isLoaded(pos.pos())) return null;

        var state = targetLevel.getBlockState(pos.pos());
        Direction facing = state.getValue(RouterBlock.FACING);
        return targetLevel.getCapability(Capabilities.Item.BLOCK, pos.pos().relative(facing), facing.getOpposite());
    }
}