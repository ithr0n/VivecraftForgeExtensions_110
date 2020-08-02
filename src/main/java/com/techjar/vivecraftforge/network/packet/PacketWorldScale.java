package com.techjar.vivecraftforge.network.packet;

import java.util.function.Supplier;

import com.techjar.vivecraftforge.network.IPacket;
import com.techjar.vivecraftforge.util.PlayerTracker;
import com.techjar.vivecraftforge.util.VRPlayerData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketWorldScale implements IPacket {
	public float worldScale;

	public PacketWorldScale() {
	}

	@Override
	public void encode(final PacketBuffer buffer) {
		buffer.writeFloat(worldScale);
	}

	@Override
	public void decode(final PacketBuffer buffer) {
		worldScale = buffer.readFloat();
	}

	@Override
	public void handleClient(final Supplier<NetworkEvent.Context> context) {
	}

	@Override
	public void handleServer(final Supplier<NetworkEvent.Context> context) {
		ServerPlayerEntity player = context.get().getSender();
		if (!PlayerTracker.hasPlayerData(player))
			return;
		context.get().enqueueWork(() -> {
			VRPlayerData data = PlayerTracker.getPlayerData(player, true);
			data.worldScale = worldScale;
		});
	}
}
