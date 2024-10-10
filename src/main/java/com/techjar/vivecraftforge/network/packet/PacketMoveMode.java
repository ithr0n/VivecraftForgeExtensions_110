package com.techjar.vivecraftforge.network.packet;

import com.techjar.vivecraftforge.network.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class PacketMoveMode implements IPacket {
	public boolean freeMove;

	public PacketMoveMode() {
	}

	public PacketMoveMode(boolean freeMove) {
		this.freeMove = freeMove;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		//buffer.writeBoolean(freeMove);
	}

	@Override
	public void decode(final FriendlyByteBuf buffer) {
		//freeMove = buffer.readBoolean();
	}

	@Override
	public void handleClient(final CustomPayloadEvent.Context context) {
	}

	@Override
	public void handleServer(final CustomPayloadEvent.Context context) {
		/*ServerPlayerEntity player = context.get().getSender();
		context.get().enqueueWork(() -> {
			VRPlayerData data = PlayerTracker.getPlayerData(player, true);
			data.freeMove = freeMove;
		});*/
	}
}
