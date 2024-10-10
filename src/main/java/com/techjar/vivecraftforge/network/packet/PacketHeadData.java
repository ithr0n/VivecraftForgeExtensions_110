package com.techjar.vivecraftforge.network.packet;

import com.techjar.vivecraftforge.network.IPacket;
import com.techjar.vivecraftforge.util.PlayerTracker;
import com.techjar.vivecraftforge.util.VRPlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class PacketHeadData implements IPacket {
	public boolean seated;
	public float posX;
	public float posY;
	public float posZ;
	public float rotW;
	public float rotX;
	public float rotY;
	public float rotZ;

	public PacketHeadData() {
	}

	public PacketHeadData(boolean seated, float posX, float posY, float posZ, float rotW, float rotX, float rotY, float rotZ) {
		this.seated = seated;
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
		this.rotW = rotW;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeBoolean(seated);
		buffer.writeFloat(posX);
		buffer.writeFloat(posY);
		buffer.writeFloat(posZ);
		buffer.writeFloat(rotW);
		buffer.writeFloat(rotX);
		buffer.writeFloat(rotY);
		buffer.writeFloat(rotZ);
	}

	@Override
	public void decode(final FriendlyByteBuf buffer) {
		seated = buffer.readBoolean();
		posX = buffer.readFloat();
		posY = buffer.readFloat();
		posZ = buffer.readFloat();
		rotW = buffer.readFloat();
		rotX = buffer.readFloat();
		rotY = buffer.readFloat();
		rotZ = buffer.readFloat();
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
			data.seated = seated;
			VRPlayerData.ObjectInfo info = data.head;
			info.posX = posX;
			info.posY = posY;
			info.posZ = posZ;
			info.rotW = rotW;
			info.rotX = rotX;
			info.rotY = rotY;
			info.rotZ = rotZ;
		});
	}
}
