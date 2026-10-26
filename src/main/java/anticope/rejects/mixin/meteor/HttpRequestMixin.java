package anticope.rejects.mixin.meteor;

import anticope.rejects.utils.RejectsConfig;
import java.net.http.HttpRequest.Builder;
import meteordevelopment.meteorclient.utils.network.Http.Request;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
   value = {Request.class},
   remap = false
)
public class HttpRequestMixin {
   @Redirect(
      method = {"<init>"},
      at = @At(
         value = "INVOKE",
         target = "Ljava/net/http/HttpRequest$Builder;header(Ljava/lang/String;Ljava/lang/String;)Ljava/net/http/HttpRequest$Builder;"
      )
   )
   private Builder onAddUAHeader(Builder builder, String userAgent, String value) {
      return RejectsConfig.get().httpUserAgent.isBlank() ? builder : builder.header("User-Agent", value);
   }
}
