package com.techjar.vivecraftforge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public interface IPacket {
	void encode(final FriendlyByteBuf buffer);

	void decode(final FriendlyByteBuf buffer);

	void handleClient(final CustomPayloadEvent.Context context);

	void handleServer(final CustomPayloadEvent.Context context);
}
