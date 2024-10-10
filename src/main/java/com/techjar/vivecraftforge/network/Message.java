package com.techjar.vivecraftforge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class Message<T extends IPacket> {
	private final Class<T> tClass;

	public Message(Class<T> tClass) {
		this.tClass = tClass;
	}

	public Class<T> getPacketClass() {
		return tClass;
	}

	public final void encode(T packet, FriendlyByteBuf buffer) {
		packet.encode(buffer);
	}

	public final T decode(FriendlyByteBuf buffer) {
		T packet;
		try {
			//packet = tClass.newInstance();
			packet = tClass.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("instantiating packet", e);
		}

		packet.decode(buffer);
		return packet;
	}

	public final void handle(T packet, CustomPayloadEvent.Context context) {
		if (context.isServerSide())
			packet.handleServer(context);
		else
			packet.handleClient(context);
		context.setPacketHandled(true);
	}
}
