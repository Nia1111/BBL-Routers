package com.benbenlaw.routers.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record StockFilter(ItemStack stack, int amount) {

    public static final Codec<StockFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("stack").forGetter(StockFilter::stack),
            Codec.INT.fieldOf("amount").forGetter(StockFilter::amount)
    ).apply(instance, StockFilter::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StockFilter> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, StockFilter::stack,
            ByteBufCodecs.INT, StockFilter::amount,
            StockFilter::new
    );

}
