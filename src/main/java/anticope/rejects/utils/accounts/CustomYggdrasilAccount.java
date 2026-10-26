package anticope.rejects.utils.accounts;

import anticope.rejects.MeteorRejectsAddon;
import com.mojang.authlib.exceptions.AuthenticationException;
import java.net.Proxy;
import meteordevelopment.meteorclient.systems.accounts.Account;
import meteordevelopment.meteorclient.systems.accounts.AccountType;
import meteordevelopment.meteorclient.utils.misc.NbtException;
import net.minecraft.client.User;
import net.minecraft.nbt.CompoundTag;

public class CustomYggdrasilAccount extends Account<CustomYggdrasilAccount> {
   private String password;
   private String server;

   public CustomYggdrasilAccount(String name, String password, String server) {
      super(AccountType.Cracked, name);
      this.password = password;
      this.server = server;
   }

   public boolean fetchInfo() {
      try {
         User session = CustomYggdrasilLogin.login(this.name, this.password, this.server);
         this.cache.username = session.getName();
         this.cache.uuid = session.getProfileId().toString();
         return true;
      } catch (AuthenticationException var2) {
         return false;
      }
   }

   public boolean login() {
      try {
         CustomYggdrasilLogin.LocalYggdrasilAuthenticationService service = new CustomYggdrasilLogin.LocalYggdrasilAuthenticationService(
            Proxy.NO_PROXY, this.server
         );
         new CustomYggdrasilLogin.LocalYggdrasilMinecraftSessionService(service, service.server);
         applyLoginEnvironment(service);
         User session = CustomYggdrasilLogin.login(this.name, this.password, this.server);
         setSession(session);
         this.cache.username = session.getName();
         this.cache.loadHead();
         return true;
      } catch (AuthenticationException var4) {
         if (!var4.getMessage().contains("Invalid username or password") && !var4.getMessage().contains("account migrated")) {
            MeteorRejectsAddon.LOG.error("Failed to contact the authentication server.");
         } else {
            MeteorRejectsAddon.LOG.error("Wrong password.");
         }

         return false;
      }
   }

   public CompoundTag toTag() {
      CompoundTag tag = super.toTag();
      tag.putString("password", this.password);
      tag.putString("server", this.server);
      return tag;
   }

   public CustomYggdrasilAccount fromTag(CompoundTag tag) {
      super.fromTag(tag);
      if (!tag.contains("password")) {
         throw new NbtException();
      } else {
         this.password = tag.getString("password").orElse("");
         this.server = tag.getString("server").orElse("");
         return this;
      }
   }

   public boolean equals(Object o) {
      return !(o instanceof CustomYggdrasilAccount) ? false : ((CustomYggdrasilAccount)o).name.equals(this.name);
   }
}
