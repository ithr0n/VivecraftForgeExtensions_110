package com.techjar.vivecraftforge.network.packet;

import com.techjar.vivecraftforge.Config;
import com.techjar.vivecraftforge.network.IPacket;
import com.techjar.vivecraftforge.util.BlockListMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.List;

/*
 * For whatever reason this uses a serializer instead of just
 * packing the data into the buffer.
 */
public class PacketClimbing implements IPacket {
	public BlockListMode blockListMode;
	public List<? extends String> blockList;

	public PacketClimbing() {
	}

	public PacketClimbing(BlockListMode blockListMode, List<? extends String> blockList) {
		this.blockListMode = blockListMode;
		this.blockList = blockList;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeByte(1); // allow climbey
		buffer.writeByte(Config.blockListMode.get().ordinal());
		for (String s : Config.blockList.get()) {
			buffer.writeUtf(s);
		}
	}

	@Override
	public void decode(final FriendlyByteBuf buffer) {
	}

	@Override
	public void handleClient(final CustomPayloadEvent.Context context) {
	}

	@Override
	public void handleServer(final CustomPayloadEvent.Context context) {
		ServerPlayer player = context.getSender();
		player.fallDistance = 0;
		player.connection.aboveGroundTickCount = 0;
	}
}
