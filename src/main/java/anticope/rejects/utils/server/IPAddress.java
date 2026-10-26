package anticope.rejects.utils.server;

public class IPAddress {
   private final int[] octets;
   private final int port;

   public IPAddress(int o1, int o2, int o3, int o4, int port) {
      this.octets = new int[]{o1, o2, o3, o4};
      this.port = port;
   }

   public IPAddress(int[] octets, int port) {
      this.octets = octets;
      this.port = port;
   }

   public static IPAddress fromText(String ip) {
      String[] sections = ip.split(":");
      if (sections.length >= 1 && sections.length <= 2) {
         int port = 25565;
         if (sections.length == 2) {
            try {
               port = Integer.parseInt(sections[1].trim());
            } catch (NumberFormatException var8) {
               return null;
            }
         }

         int[] octets = new int[4];
         String[] address = sections[0].trim().split("\\.");
         if (address.length != 4) {
            return null;
         } else {
            for (int i = 0; i < 4; i++) {
               try {
                  octets[i] = Integer.parseInt(address[i].trim());
               } catch (NumberFormatException var7) {
                  return null;
               }
            }

            return new IPAddress(octets, port);
         }
      } else {
         return null;
      }
   }

   @Override
   public boolean equals(Object o) {
      if (o instanceof IPAddress other) {
         if (this.octets.length != other.octets.length) {
            return false;
         } else if (this.port != other.port) {
            return false;
         } else {
            for (int i = 0; i < this.octets.length; i++) {
               if (this.octets[i] != other.octets[i]) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      assert this.octets.length == 4;

      int hash = 43;
      hash = hash * 59 + this.octets[0];
      hash = hash * 83 + this.octets[1];
      hash = hash * 71 + this.octets[2];
      hash = hash * 17 + this.octets[3];
      return hash * 31 + this.port;
   }

   @Override
   public String toString() {
      if (this.octets.length == 0) {
         return null;
      } else {
         StringBuilder result = new StringBuilder();
         result.append(this.octets[0]);

         for (int i = 1; i < this.octets.length; i++) {
            result.append('.').append(this.octets[i]);
         }

         result.append(':').append(this.port);
         return result.toString();
      }
   }
}
