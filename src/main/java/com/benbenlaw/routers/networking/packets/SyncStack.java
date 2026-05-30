package com.benbenlaw.routers.networking.packets;

import com.benbenlaw.routers.Routers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record SyncStack(ItemStack stack) implements CustomPacketPayload {

    public static final Type<SyncStack> TYPE = new Type<>(Routers.identifier("sync_stack"));

    public static final IPayloadHandler<SyncStack> HANDLER = (packet, context) -> {

        context.player().setItemInHand(InteractionHand.MAIN_HAND, packet.stack());
    };

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncStack> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, SyncStack::stack,
            SyncStack::new
    );

    @Override
    public Type<SyncStack> type() {
        return TYPE;
    }
}