package com.techjar.vivecraftforge.network.packet;

import com.techjar.vivecraftforge.network.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class PacketRequestData implements IPacket {
	public PacketRequestData() {
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
	}

	@Override
	public void decode(final FriendlyByteBuf buffer) {
	}

	@Override
	public void handleClient(final CustomPayloadEvent.Context context) {
	}

	@Override
	public void handleServer(final CustomPayloadEvent.Context context) {
	}
}
