package com.seedfinding.mcfeature.loot;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.Feature;
import com.seedfinding.mcfeature.GenerationContext;
import com.seedfinding.mcfeature.loot.item.ItemStack;
import com.seedfinding.mcfeature.structure.generator.Generator;
import com.seedfinding.mcfeature.structure.generator.Generators;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public interface ILoot {
   default List<ChestContent> getLoot(long structureSeed, Generator generator, boolean indexed) {
      return this.getLoot(structureSeed, generator, new ChunkRand(), indexed);
   }

   default List<ChestContent> getLoot(long structureSeed, Generator generator, ChunkRand rand, boolean indexed) {
      if (!this.isCorrectGenerator(generator)) {
         return null;
      } else {
         List<Pair<Generator.ILootType, BPos>> lootPositions = generator.getLootPos();
         HashMap<CPos, LinkedList<Pair<Generator.ILootType, BPos>>> posLinkedListHashMap = new HashMap<>();

         for (Pair<Generator.ILootType, BPos> lootPos : lootPositions) {
            if (lootPos.getFirst().getLootTable(this.getVersion()) != null) {
               BPos pos = lootPos.getSecond();
               CPos cPos = pos.toChunkPos();
               posLinkedListHashMap.computeIfAbsent(cPos, k -> new LinkedList<>()).add(lootPos);
            }
         }

         HashMap<Generator.ILootType, List<ILoot.ChestData>> chestDataHashMap = new HashMap<>();

         for (CPos cPos : posLinkedListHashMap.keySet()) {
            LinkedList<Pair<Generator.ILootType, BPos>> lootTypes = posLinkedListHashMap.get(cPos);
            int index = 0;

            for (Pair<Generator.ILootType, BPos> lootType : lootTypes) {
               chestDataHashMap.computeIfAbsent(lootType.getFirst(), k -> new ArrayList<>())
                  .add(new ILoot.ChestData(index, cPos, lootType.getSecond(), lootTypes.size()));
               index++;
            }
         }

         List<ChestContent> result = new ArrayList<>();

         for (Generator.ILootType lootType : chestDataHashMap.keySet()) {
            for (ILoot.ChestData chestData : chestDataHashMap.get(lootType)) {
               CPos chunkChestPos = chestData.getChunkPos();
               rand.setDecoratorSeed(structureSeed, chunkChestPos.getX() * 16, chunkChestPos.getZ() * 16, this.getDecorationSalt(), this.getVersion());
               ILoot.SpecificCalls calls = this.getSpecificCalls();
               if (calls != null) {
                  calls.run(generator, rand);
               }

               if (this.shouldAdvanceInChunks()) {
                  rand.advance(chestData.getNumberInChunk() * 2L);
               }

               rand.advance(chestData.getIndex() * 2L);
               LootContext context = new LootContext(rand.nextLong(), this.getVersion());
               LootTable lootTable = lootType.getLootTable(this.getVersion());
               List<ItemStack> loot = (List<ItemStack>)(indexed ? lootTable.generateIndexed(context) : lootTable.generate(context));
               result.add(new ChestContent(lootType, loot, chestData.getPos(), indexed));
            }
         }

         return result;
      }
   }

   default List<ChestContent> getLootAtPos(long worldSeed, CPos pos, ChunkRand rand, boolean indexed) {
      if (!(this instanceof Feature)) {
         return null;
      } else {
         Feature<?, ?> feature = (Feature<?, ?>)this;
         Generator.GeneratorFactory<?> factory = Generators.get(feature.getClass());
         if (factory == null) {
            return null;
         } else {
            Generator generator = factory.create(this.getVersion());
            if (generator == null) {
               return null;
            } else {
               GenerationContext.Context context = feature.getContext(worldSeed);
               if (context == null) {
                  return null;
               } else {
                  return !generator.generate(context.getGenerator(), pos) ? null : this.getLoot(worldSeed, generator, rand, indexed);
               }
            }
         }
      }
   }

   @Deprecated
   default HashMap<Generator.ILootType, List<List<ItemStack>>> getLootEx(long structureSeed, Generator generator, ChunkRand rand, boolean indexed) {
      HashMap<Generator.ILootType, List<List<ItemStack>>> res = new HashMap<>();

      for (ChestContent chestContent : this.getLoot(structureSeed, generator, rand, indexed)) {
         res.computeIfAbsent(chestContent.getLootType(), e -> new ArrayList<>()).add(chestContent.getItems());
      }

      return res;
   }

   default boolean shouldAdvanceInChunks() {
      return true;
   }

   int getDecorationSalt();

   boolean isCorrectGenerator(Generator var1);

   MCVersion getVersion();

   ILoot.SpecificCalls getSpecificCalls();

   public static class ChestData {
      private final int index;
      private final CPos chunkPos;
      private final BPos pos;
      private final int numberInChunk;

      public ChestData(int index, CPos chunkPos, BPos pos, int numberInChunk) {
         this.index = index;
         this.chunkPos = chunkPos;
         this.pos = pos;
         this.numberInChunk = numberInChunk;
      }

      public BPos getPos() {
         return this.pos;
      }

      public CPos getChunkPos() {
         return this.chunkPos;
      }

      public int getIndex() {
         return this.index;
      }

      public int getNumberInChunk() {
         return this.numberInChunk;
      }

      @Override
      public String toString() {
         return "ChestData{index=" + this.index + ", cPos=" + this.chunkPos + ", bpos=" + this.pos + ", numberInChunk=" + this.numberInChunk + '}';
      }
   }

   @FunctionalInterface
   public interface SpecificCalls {
      void run(Generator var1, ChunkRand var2);
   }
}
