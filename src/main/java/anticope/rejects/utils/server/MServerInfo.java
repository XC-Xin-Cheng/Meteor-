package anticope.rejects.utils.server;

import java.util.Collections;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class MServerInfo {
   public String name;
   public String address;
   public String playerCountLabel;
   public int playerCount;
   public int playercountMax;
   public String label;
   public long ping;
   public int protocolVersion = SharedConstants.getCurrentVersion().protocolVersion();
   public String version = null;
   public List<Component> playerListSummary = Collections.emptyList();
   @Nullable
   private byte[] icon;

   public MServerInfo(String name, String address) {
      this.name = name;
      this.address = address;
   }

   @Nullable
   public byte[] getIcon() {
      return this.icon;
   }

   public void setIcon(@Nullable byte[] bytes) {
      this.icon = bytes;
   }
}
