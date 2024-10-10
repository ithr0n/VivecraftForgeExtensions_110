package com.techjar.vivecraftforge.network.packet;

import com.techjar.vivecraftforge.network.IPacket;
import com.techjar.vivecraftforge.util.PlayerTracker;
import com.techjar.vivecraftforge.util.VRPlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class PacketDraw implements IPacket {
	public float drawDist;

	public PacketDraw() {
	}

	public PacketDraw(float drawDist) {
		this.drawDist = drawDist;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeFloat(drawDist);
	}

	@Override
	public void decode(final FriendlyByteBuf buffer) {
		drawDist = buffer.readFloat();
	}

	@Override
	public void handleClient(final CustomPayloadEvent.Context context) {
	}

	@Override
	public void handleServer(final CustomPayloadEvent.Context context) {
		ServerPlayer player = context.getSender();
		context.enqueueWork(() -> {
			if (!PlayerTracker.hasPlayerData(player))
				return;
			VRPlayerData data = PlayerTracker.getPlayerData(player, true);
			data.bowDraw = drawDist;
		});
	}
}
