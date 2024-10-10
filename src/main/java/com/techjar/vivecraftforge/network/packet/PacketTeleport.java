package com.techjar.vivecraftforge.network.packet;

import com.techjar.vivecraftforge.Config;
import com.techjar.vivecraftforge.network.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class PacketTeleport implements IPacket {
	public float posX;
	public float posY;
	public float posZ;

	public PacketTeleport() {
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
	}

	@Override
	public void decode(final FriendlyByteBuf buffer) {
		posX = buffer.readFloat();
		posY = buffer.readFloat();
		posZ = buffer.readFloat();
	}

	@Override
	public void handleClient(final CustomPayloadEvent.Context context) {
	}

	@Override
	public void handleServer(final CustomPayloadEvent.Context context) {
		if (Config.teleportEnabled.get()) {
			ServerPlayer player = context.getSender();
			context.enqueueWork(() -> player.moveTo(posX, posY, posZ, player.getYRot(), player.getXRot()));
		}
	}
}
