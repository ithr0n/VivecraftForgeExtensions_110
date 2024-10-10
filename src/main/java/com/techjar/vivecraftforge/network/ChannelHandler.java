package com.techjar.vivecraftforge.network;

import com.techjar.vivecraftforge.VivecraftForge;
import com.techjar.vivecraftforge.util.LogHelper;

import com.techjar.vivecraftforge.network.packet.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class ChannelHandler {
	private static final SimpleChannel CHANNEL = ChannelBuilder.named(
			ResourceLocation.fromNamespaceAndPath("vivecraft", "data"))
			.serverAcceptedVersions((status, version) -> true)
			.clientAcceptedVersions((status, version) -> true)
			.networkProtocolVersion(1)
			.simpleChannel();

	public static void init() {
		addDiscriminator(0, new Message<>(PacketVersion.class));
		addDiscriminator(1, new Message<>(PacketRequestData.class));
		addDiscriminator(2, new Message<>(PacketHeadData.class));
		addDiscriminator(3, new Message<>(PacketController0Data.class));
		addDiscriminator(4, new Message<>(PacketController1Data.class));
		addDiscriminator(5, new Message<>(PacketWorldScale.class));
		addDiscriminator(6, new Message<>(PacketDraw.class));
		addDiscriminator(7, new Message<>(PacketMoveMode.class));
		addDiscriminator(8, new Message<>(PacketUberPacket.class));
		addDiscriminator(9, new Message<>(PacketTeleport.class));
		addDiscriminator(10, new Message<>(PacketClimbing.class));
		addDiscriminator(11, new Message<>(PacketSettingOverride.class));
		addDiscriminator(12, new Message<>(PacketHeight.class));
		addDiscriminator(13, new Message<>(PacketActiveHand.class));
		addDiscriminator(14, new Message<>(PacketCrawl.class));

		LogHelper.debug("Networking initialized");
	}

	private static <T extends IPacket> void addDiscriminator(int d, Message<T> message) {
		CHANNEL.messageBuilder(message.getPacketClass(), d)
				.encoder(message::encode)
				.decoder(message::decode)
				.consumerMainThread(message::handle);
	}

	public static void sendToAll(IPacket message) {
		CHANNEL.send(message, PacketDistributor.ALL.noArg());
	}

	public static void sendTo(IPacket message, ServerPlayer player) {
		CHANNEL.send(message, PacketDistributor.PLAYER.with(player));
	}

	public static void sendToAllAround(IPacket message, PacketDistributor.TargetPoint point) {
		CHANNEL.send(message, PacketDistributor.NEAR.with(point));
	}

	public static void sendToAllTrackingEntity(IPacket message, ServerPlayer player) {
		CHANNEL.send(message, PacketDistributor.TRACKING_ENTITY.with(player));
	}

	public static void sendToAllTrackingEntityAndSelf(IPacket message, ServerPlayer player) {
		CHANNEL.send(message, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(player));
	}

	public static void sendToAllTrackingChunk(IPacket message, LevelChunk chunk) {
		CHANNEL.send(message, PacketDistributor.TRACKING_CHUNK.with(chunk));
	}

	public static void sendToAllInDimension(IPacket message, ResourceKey<Level> dimension) {
		CHANNEL.send(message, PacketDistributor.DIMENSION.with(dimension));
	}

	public static void sendToServer(IPacket message) {
		CHANNEL.send(message, PacketDistributor.SERVER.noArg());
	}
}
