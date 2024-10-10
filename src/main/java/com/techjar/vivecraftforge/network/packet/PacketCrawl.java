package com.techjar.vivecraftforge.network.packet;

import com.techjar.vivecraftforge.Config;
import com.techjar.vivecraftforge.network.IPacket;
import com.techjar.vivecraftforge.util.PlayerTracker;
import com.techjar.vivecraftforge.util.VRPlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class PacketCrawl implements IPacket {
	public boolean crawling;

	public PacketCrawl() {
	}

	public PacketCrawl(boolean crawling) {
		this.crawling = crawling;
	}

	@Override
	public void encode(FriendlyByteBuf buffer) {
	}

	@Override
	public void decode(FriendlyByteBuf buffer) {
		crawling = buffer.readBoolean();
	}

	@Override
	public void handleClient(final CustomPayloadEvent.Context context) {
	}

	@Override
	public void handleServer(final CustomPayloadEvent.Context context) {
		if (Config.crawlingEnabled.get()) {
			ServerPlayer player = context.getSender();
			context.enqueueWork(() -> {
				if (!PlayerTracker.hasPlayerData(player))
					return;
				VRPlayerData data = PlayerTracker.getPlayerData(player, true);
				data.crawling = crawling;
				if (data.crawling)
					player.setPose(Pose.SWIMMING);
			});
		}
	}
}
