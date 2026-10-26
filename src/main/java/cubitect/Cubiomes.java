package cubitect;

import org.fusesource.hawtjni.runtime.JniClass;
import org.fusesource.hawtjni.runtime.Library;

@JniClass
public class Cubiomes {
   private static final Library LIBRARY = new Library("cubiomes", Cubiomes.class);

   public static native int main();

   private static native long getNearestStructure(int var0, int var1, int var2, long var3, int var5);

   private static native long getNearestStronghold(int var0, int var1, long var2, int var4);

   public static Cubiomes.Pos GetNearestStructure(Cubiomes.StructureType structType, int x, int z, Long seed, Cubiomes.MCVersion mc_version) {
      long result;
      if (structType == Cubiomes.StructureType.Stronghold) {
         result = getNearestStronghold(x, z, seed, mc_version.ordinal());
      } else {
         result = getNearestStructure(structType.ordinal(), x, z, seed, mc_version.ordinal());
      }

      if (result == -1L) {
         return null;
      } else {
         Cubiomes.Pos pos = new Cubiomes.Pos();
         pos.x = (int)(result >> 32);
         pos.z = (int)(result & -1L);
         return pos;
      }
   }

   static {
      LIBRARY.load();
   }

   public static enum MCVersion {
      MC_UNDEF,
      MC_B1_7,
      MC_B1_8,
      MC_1_0,
      MC_1_1,
      MC_1_2,
      MC_1_3,
      MC_1_4,
      MC_1_5,
      MC_1_6,
      MC_1_7,
      MC_1_8,
      MC_1_9,
      MC_1_10,
      MC_1_11,
      MC_1_12,
      MC_1_13,
      MC_1_14,
      MC_1_15,
      MC_1_16,
      MC_1_17,
      MC_1_18,
      MC_1_19_2,
      MC_1_19,
      MC_1_20;
   }

   public static class Pos {
      public int x;
      public int z;
      public int y;
   }

   public static enum StructureType {
      Feature,
      Desert_Pyramid,
      Jungle_Temple,
      Swamp_Hut,
      Igloo,
      Village,
      Ocean_Ruin,
      Shipwreck,
      Monument,
      Mansion,
      Outpost,
      Ruined_Portal,
      Ruined_Portal_N,
      Ancient_City,
      Treasure,
      Mineshaft,
      Desert_Well,
      Geode,
      Fortress,
      Bastion,
      End_City,
      End_Gateway,
      Trail_Ruin,
      Slime_Chunk,
      Stronghold;
   }
}
