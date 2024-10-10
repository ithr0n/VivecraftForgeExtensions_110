package com.techjar.vivecraftforge.eventhandler;

import com.techjar.vivecraftforge.Config;
import com.techjar.vivecraftforge.entity.ai.goal.VRCreeperSwellGoal;
import com.techjar.vivecraftforge.entity.ai.goal.VREndermanFindPlayerGoal;
import com.techjar.vivecraftforge.entity.ai.goal.VREndermanStareGoal;
import com.techjar.vivecraftforge.network.ChannelHandler;
import com.techjar.vivecraftforge.network.packet.PacketUberPacket;
import com.techjar.vivecraftforge.util.AimFixHandler;
import com.techjar.vivecraftforge.util.LogHelper;
import com.techjar.vivecraftforge.util.PlayerTracker;
import com.techjar.vivecraftforge.util.Util;
import com.techjar.vivecraftforge.util.VRPlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.network.Connection;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class EventHandlerServer {
	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			PlayerTracker.tick();
			PlayerList playerList = ServerLifecycleHooks.getCurrentServer().getPlayerList();
			int viewDist = playerList.getViewDistance();
			float range = Mth.clamp(viewDist / 8.0F, 1.0F, 2.5F) * 64.0F; // This is how the client determines entity render distance
			for (Map.Entry<UUID, VRPlayerData> entry : PlayerTracker.players.entrySet()) {
				ServerPlayer player = playerList.getPlayer(entry.getKey());
				if (player != null) {
					PacketUberPacket packet = PlayerTracker.getPlayerDataPacket(entry.getKey(), entry.getValue());
					ChannelHandler.sendToAllTrackingEntity(packet, player);
				}
			}
		}
	}

	@SubscribeEvent
	public void onAttackEntity(AttackEntityEvent event) {
		if (event.getTarget() instanceof Player) {
			Player player = event.getEntity();
			Player target = (Player)event.getTarget();
			if (PlayerTracker.hasPlayerData(player)) {
				VRPlayerData data = PlayerTracker.getPlayerData(player);
				if (data.seated) { // Seated VR vs...
					if (PlayerTracker.hasPlayerData(target)) {
						VRPlayerData targetData = PlayerTracker.getPlayerData(target);
						if (targetData.seated) { // ...seated VR
							if (!Config.seatedVrVsSeatedVR.get()) event.setCanceled(true);
						} else { // ...VR
							if (!Config.vrVsSeatedVR.get()) event.setCanceled(true);
						}
					} else { // ...non-VR
						if (!Config.seatedVrVsNonVR.get()) event.setCanceled(true);
					}
				} else { // VR vs...
					if (PlayerTracker.hasPlayerData(target)) {
						VRPlayerData targetData = PlayerTracker.getPlayerData(target);
						if (targetData.seated) { // ...seated VR
							if (!Config.vrVsSeatedVR.get()) event.setCanceled(true);
						} else { // ...VR
							if (!Config.vrVsVR.get()) event.setCanceled(true);
						}
					} else { // ...non-VR
						if (!Config.vrVsNonVR.get()) event.setCanceled(true);
					}
				}
			} else { // Non-VR vs...
				if (PlayerTracker.hasPlayerData(target)) {
					VRPlayerData targetData = PlayerTracker.getPlayerData(target);
					if (targetData.seated) { // ...seated VR
						if (!Config.seatedVrVsNonVR.get()) event.setCanceled(true);
					} else { // ...VR
						if (!Config.vrVsNonVR.get()) event.setCanceled(true);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onArrowLoose(ArrowLooseEvent event) {
		Player player = event.getEntity();
		VRPlayerData data = PlayerTracker.getPlayerData(player);
		if (data != null && !data.seated && data.bowDraw > 0) {
			LogHelper.debug("Bow draw: " + data.bowDraw);
			event.setCharge(Math.round(data.bowDraw * 20));
		}
	}

	@SubscribeEvent
	public void onLivingHurt(LivingHurtEvent event) {
		LivingEntity target = event.getEntity();
		DamageSource source = event.getSource();
		if (source.getDirectEntity() instanceof Arrow && source.getEntity() instanceof Player) {
			Arrow arrow = (Arrow) source.getDirectEntity();
			Player attacker = (Player)source.getEntity();
			if (PlayerTracker.hasPlayerData(attacker)) {
				VRPlayerData data = PlayerTracker.getPlayerData(attacker);
				boolean headshot = Util.isHeadshot(target, arrow);
				if (data.seated) {
					if (headshot) event.setAmount(event.getAmount() * Config.bowSeatedHeadshotMul.get().floatValue());
					else event.setAmount(event.getAmount() * Config.bowSeatedMul.get().floatValue());
				} else {
					if (headshot) event.setAmount(event.getAmount() * Config.bowStandingHeadshotMul.get().floatValue());
					else event.setAmount(event.getAmount() * Config.bowStandingMul.get().floatValue());
				}
			}
		}
	}

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinLevelEvent event) {
		if (event.getEntity() instanceof ServerPlayer) {
			final ServerPlayer player = (ServerPlayer)event.getEntity();
			if (Config.vrOnly.get() && !player.hasPermissions(2)) {
				Util.scheduler.schedule(() -> {
					ServerLifecycleHooks.getCurrentServer().submit(() -> {
						if (player.connection.getConnection().isConnected() && !PlayerTracker.hasPlayerData(player)) {
							player.sendSystemMessage(Component.literal(Config.vrOnlyKickMessage.get()));
							player.sendSystemMessage(Component.literal("If this is not a VR client, you will be kicked in " + Config.vrOnlyKickDelay.get() + " seconds."));
							Util.scheduler.schedule(() -> {
								ServerLifecycleHooks.getCurrentServer().submit(() -> {
									if (player.connection.getConnection().isConnected() && !PlayerTracker.hasPlayerData(player)) {
										player.connection.disconnect(Component.literal(Config.vrOnlyKickMessage.get()));
									}
								});
							}, Math.round(Config.vrOnlyKickDelay.get() * 1000), TimeUnit.MILLISECONDS);
						}
					});
				}, 1000, TimeUnit.MILLISECONDS);
			}
		} else if (event.getEntity() instanceof Projectile) {
			Projectile projectile = (Projectile)event.getEntity();
			if (!(projectile.getOwner() instanceof Player))
				return;
			Player shooter = (Player)projectile.getOwner();
			if (!PlayerTracker.hasPlayerData(shooter))
				return;

			boolean arrow = projectile instanceof AbstractArrow && !(projectile instanceof ThrownTrident);
			VRPlayerData data = PlayerTracker.getPlayerDataAbsolute(shooter);
			Vec3 pos = data.getController(data.activeHand).getPos();
			Vec3 aim = data.getController(data.activeHand).getRot().multiply(new Vec3(0, 0, -1));

			if (arrow && !data.seated && data.bowDraw > 0) {
				pos = data.getController(0).getPos();
				aim = data.getController(1).getPos().subtract(pos).normalize();
			}

			pos = pos.add(aim.scale(0.6));
			double vel = projectile.getDeltaMovement().length();
			projectile.setPos(pos.x, pos.y, pos.z);
			projectile.shoot(aim.x, aim.y, aim.z, (float)vel, 0.0f);

			Vec3 shooterMotion = shooter.getDeltaMovement();
			projectile.setDeltaMovement(projectile.getDeltaMovement().add(shooterMotion.x, shooter.onGround() ? 0.0 : shooterMotion.y, shooterMotion.z));

			LogHelper.debug("Projectile direction: {}", aim);
			LogHelper.debug("Projectile velocity: {}", vel);
		} else if (event.getEntity() instanceof Creeper) {
			Creeper creeper = (Creeper)event.getEntity();
			Util.replaceAIGoal(creeper, creeper.goalSelector, SwellGoal.class, () -> new VRCreeperSwellGoal(creeper));
		} else if (event.getEntity() instanceof EnderMan) {
			EnderMan enderman = (EnderMan)event.getEntity();
			Util.replaceAIGoal(enderman, enderman.goalSelector, EnderMan.EndermanFreezeWhenLookedAt.class, () -> new VREndermanStareGoal(enderman));
			Util.replaceAIGoal(enderman, enderman.targetSelector, EnderMan.EndermanLookForPlayerGoal.class, () -> new VREndermanFindPlayerGoal(enderman, enderman::isAngryAt));
		}
	}

	@SubscribeEvent
	public void onItemToss(ItemTossEvent event) {
		if (!PlayerTracker.hasPlayerData(event.getPlayer()))
			return;

		VRPlayerData data = PlayerTracker.getPlayerDataAbsolute(event.getPlayer());
		ItemEntity item = event.getEntity();

		Vec3 pos = data.getController(0).getPos();
		Vec3 aim = data.getController(0).getRot().multiply(new Vec3(0, 0, -1));
		Vec3 aimUp = data.getController(0).getRot().multiply(new Vec3(0, 1, 0));
		double pitch = Math.toDegrees(Math.asin(-aim.y));

		pos = pos.add(aim.scale(0.2)).subtract(aimUp.scale(0.4 * (1 - Math.abs(pitch) / 90)));
		double vel = 0.3;
		item.setPos(pos.x, pos.y, pos.z);
		item.setDeltaMovement(aim.scale(vel));
	}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		Connection netManager = ((ServerPlayer)event.getEntity()).connection.getConnection();
		netManager.channel().pipeline().addBefore("packet_handler", "vr_aim_fix", new AimFixHandler(netManager));
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			VRPlayerData data = PlayerTracker.getPlayerData(event.player);
			if (data != null && data.crawling)
				event.player.setPose(Pose.SWIMMING);
		}
	}
}
