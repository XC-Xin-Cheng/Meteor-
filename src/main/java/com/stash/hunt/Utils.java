package com.stash.hunt;

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownServiceException;
import javax.net.ssl.HttpsURLConnection;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Utils {
   public static int firework(Minecraft mc, boolean elytraRequired) {
      int elytraSwapSlot = -1;
      if (elytraRequired && !mc.player.getInventory().getItem(38).is(Items.ELYTRA)) {
         FindItemResult itemResult = InvUtils.findInHotbar(new Item[]{Items.ELYTRA});
         if (!itemResult.found()) {
            return -1;
         }

         elytraSwapSlot = itemResult.slot();
         InvUtils.swap(itemResult.slot(), true);
         mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
         InvUtils.swapBack();
         mc.getConnection().send(new ServerboundPlayerCommandPacket(mc.player, Action.START_FALL_FLYING));
      }

      FindItemResult itemResult = InvUtils.findInHotbar(new Item[]{Items.FIREWORK_ROCKET});
      if (!itemResult.found()) {
         return -1;
      } else {
         if (itemResult.isOffhand()) {
            mc.gameMode.useItem(mc.player, InteractionHand.OFF_HAND);
            mc.player.swing(InteractionHand.OFF_HAND);
         } else {
            InvUtils.swap(itemResult.slot(), true);
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            mc.player.swing(InteractionHand.MAIN_HAND);
            InvUtils.swapBack();
         }

         return elytraSwapSlot != -1 ? elytraSwapSlot : 200;
      }
   }

   public static void setPressed(KeyMapping key, boolean pressed) {
      key.setDown(pressed);
      Input.setKeyState(key, pressed);
   }

   public static int emptyInvSlots(Minecraft mc) {
      int airCount = 0;

      for (int i = 0; i < 36; i++) {
         if (mc.player.getInventory().getItem(i).getItem() == Items.AIR) {
            airCount++;
         }
      }

      return airCount;
   }

   public static Vec3 positionInDirection(Vec3 pos, double yaw, double distance) {
      Vec3 offset = yawToDirection(yaw).scale(distance);
      return pos.add(offset);
   }

   public static Vec3 yawToDirection(double yaw) {
      yaw = yaw * Math.PI / 180.0;
      double x = -Math.sin(yaw);
      double z = Math.cos(yaw);
      return new Vec3(x, 0.0, z);
   }

   public static double distancePointToDirection(Vec3 point, Vec3 direction, @Nullable Vec3 start) {
      if (start == null) {
         start = Vec3.ZERO;
      }

      point = point.multiply(new Vec3(1.0, 0.0, 1.0));
      start = start.multiply(new Vec3(1.0, 0.0, 1.0));
      direction = direction.multiply(new Vec3(1.0, 0.0, 1.0));
      Vec3 directionVec = point.subtract(start);
      double projectionLength = directionVec.dot(direction) / direction.lengthSqr();
      Vec3 projection = direction.scale(projectionLength);
      Vec3 perp = directionVec.subtract(projection);
      return perp.length();
   }

   public static double angleOnAxis(double yaw) {
      if (yaw < 0.0) {
         yaw += 360.0;
      }

      return Math.round(yaw / 45.0) * 45L;
   }

   public static Vec3 normalizedPositionOnAxis(Vec3 pos) {
      double angle = -Math.atan2(pos.x, pos.z);
      double angleDeg = Math.toDegrees(angle);
      return positionInDirection(new Vec3(0.0, 0.0, 0.0), angleOnAxis(angleDeg), 1.0);
   }

   public static int totalInvCount(Minecraft mc, Item item) {
      if (mc.player == null) {
         return 0;
      } else {
         int itemCount = 0;

         for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() == item) {
               itemCount += stack.getCount();
            }
         }

         return itemCount;
      }
   }

   public static float smoothRotation(double current, double target, double rotationScaling) {
      double difference = angleDifference(target, current);
      return (float)(current + difference * rotationScaling);
   }

   public static double angleDifference(double target, double current) {
      double diff = (target - current + 180.0) % 360.0 - 180.0;
      return diff < -180.0 ? diff + 360.0 : diff;
   }

   public static void sendWebhook(String webhookURL, String title, String message, String pingID, String playerName) {
      String json = "";
      json = json
         + "{\"embeds\": [{\"title\": \""
         + title
         + "\",\"description\": \""
         + message
         + "\",\"color\": 15258703,\"footer\": {\"text\": \"From: "
         + playerName
         + "\"}}]}";
      sendRequest(webhookURL, json);
      if (pingID != null) {
         json = "{\"content\": \"<@" + pingID + ">\"}";
         sendRequest(webhookURL, json);
      }
   }

   public static void sendWebhook(String webhookURL, String jsonObject, String pingID) {
      sendRequest(webhookURL, jsonObject);
      if (pingID != null) {
         jsonObject = "{\"content\": \"<@" + pingID + ">\"}";
         sendRequest(webhookURL, jsonObject);
      }
   }

   private static void sendRequest(String webhookURL, String json) {
      try {
         URL url = new URL(webhookURL);
         HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
         con.addRequestProperty("Content-Type", "application/json");
         con.addRequestProperty("User-Agent", "Mozilla");
         con.setDoOutput(true);
         con.setRequestMethod("POST");
         OutputStream stream = con.getOutputStream();
         stream.write(json.getBytes());
         stream.flush();
         stream.close();
         con.getInputStream().close();
         con.disconnect();
      } catch (UnknownServiceException | MalformedURLException var5) {
      } catch (Exception var6) {
         var6.printStackTrace();
      }
   }
}
