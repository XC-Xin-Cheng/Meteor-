package anticope.rejects.gui.screens;

import anticope.rejects.utils.GiveUtils;
import com.google.common.reflect.TypeToken;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.settings.EnumSetting.Builder;
import meteordevelopment.meteorclient.utils.network.Http;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

public class HeadScreen extends WindowScreen {
   private static final Type gsonType = (new TypeToken<List<Map<String, String>>>() {}).getType();
   private final Settings settings = new Settings();
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private static HeadScreen.Categories category = HeadScreen.Categories.Decoration;
   private final Setting<HeadScreen.Categories> categorySetting = this.sgGeneral
      .add(
         ((Builder)((Builder)((Builder)((Builder)new Builder().name("Category")).defaultValue(category)).description("Category"))
               .onChanged(v -> this.loadHeads()))
            .build()
      );

   public HeadScreen(GuiTheme theme) {
      super(theme, "Heads");
      this.loadHeads();
   }

   private void set() {
      this.clear();
      this.add(this.theme.settings(this.settings)).expandX();
      this.add(this.theme.horizontalSeparator()).expandX();
   }

   private String getCat() {
      category = (HeadScreen.Categories)this.categorySetting.get();
      return category.toString().replace("_", "-");
   }

   private void loadHeads() {
      MeteorExecutor.execute(
         () -> {
            List<Map<String, String>> res = (List<Map<String, String>>)Http.get("https://minecraft-heads.com/scripts/api.php?cat=" + this.getCat())
               .sendJson(gsonType);
            List<ItemStack> heads = new ArrayList<>();
            res.forEach(a -> {
               try {
                  heads.add(this.createHeadStack(a.get("uuid"), a.get("value"), a.get("name")));
               } catch (Exception var4) {
               }
            });
            WTable t = this.theme.table();

            for (ItemStack head : heads) {
               t.add(this.theme.item(head));
               t.add(this.theme.label(head.getHoverName().getString()));
               WButton give = (WButton)t.add(this.theme.button("Give")).widget();
               give.action = () -> {
                  try {
                     GiveUtils.giveItem(head);
                  } catch (CommandSyntaxException var2x) {
                     ChatUtils.errorPrefix("Heads", var2x.getMessage(), new Object[0]);
                  }
               };
               WButton equip = (WButton)t.add(this.theme.button("Equip")).widget();
               equip.tooltip = "Equip client-side.";
               equip.action = () -> MeteorClient.mc.player.getInventory().setItem(39, head);
               t.row();
            }

            this.set();
            this.add(t).expandX().minWidth(400.0).widget();
         }
      );
   }

   private ItemStack createHeadStack(String uuid, String value, String name) {
      ItemStack head = Items.PLAYER_HEAD.getDefaultInstance();
      CompoundTag tag = new CompoundTag();
      CompoundTag skullOwner = new CompoundTag();
      UUID parsedUuid = UUID.fromString(uuid);
      skullOwner.put(
         "Id",
         new IntArrayTag(
            new int[]{
               (int)(parsedUuid.getMostSignificantBits() >> 32),
               (int)parsedUuid.getMostSignificantBits(),
               (int)(parsedUuid.getLeastSignificantBits() >> 32),
               (int)parsedUuid.getLeastSignificantBits()
            }
         )
      );
      CompoundTag properties = new CompoundTag();
      ListTag textures = new ListTag();
      CompoundTag Value = new CompoundTag();
      Value.put("Value", StringTag.valueOf(value));
      textures.add(Value);
      properties.put("textures", textures);
      skullOwner.put("Properties", properties);
      tag.put("SkullOwner", skullOwner);
      head.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
      head.set(DataComponents.CUSTOM_NAME, Component.literal(name));
      return head;
   }

   public void initWidgets() {
   }

   public static enum Categories {
      Alphabet,
      Animals,
      Blocks,
      Decoration,
      Food_Drinks,
      Humanoid,
      Miscellaneous,
      Monsters,
      Plants;
   }
}
