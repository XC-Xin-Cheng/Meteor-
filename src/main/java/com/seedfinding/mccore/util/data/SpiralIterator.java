package com.seedfinding.mccore.util.data;

import com.seedfinding.mccore.util.math.Vec3i;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import org.jetbrains.annotations.NotNull;

public class SpiralIterator<T extends Vec3i> implements Iterable<T> {
   private final T center;
   private final T lowerBound;
   private final T upperBound;
   private final int step;
   private final SpiralIterator.Builder<T> builder;

   public SpiralIterator(T center, T lowerBound, T upperBound, int step, SpiralIterator.Builder<T> builder) {
      this.center = center;
      this.lowerBound = lowerBound;
      this.upperBound = upperBound;
      this.step = step;
      this.builder = builder;
   }

   public SpiralIterator(T center, T radius, int step, SpiralIterator.Builder<T> builder) {
      this(
         center,
         builder.build(center.getX() - radius.getX(), 0, center.getZ() - radius.getZ()),
         builder.build(center.getX() + radius.getX(), 0, center.getZ() + radius.getZ()),
         step,
         builder
      );
   }

   public SpiralIterator(T center, T radius, SpiralIterator.Builder<T> builder) {
      this(center, radius, 1, builder);
   }

   @NotNull
   @Override
   public Iterator<T> iterator() {
      return new Iterator<T>() {
         private int x = SpiralIterator.this.center.getX();
         private int z = SpiralIterator.this.center.getZ();
         private float n = 1.0F;
         private int i = 0;
         private int j = 0;

         @Override
         public boolean hasNext() {
            return this.x >= SpiralIterator.this.lowerBound.getX()
               && this.x <= SpiralIterator.this.upperBound.getX()
               && this.z >= SpiralIterator.this.lowerBound.getZ()
               && this.z <= SpiralIterator.this.upperBound.getZ();
         }

         public T next() {
            T returnValue = SpiralIterator.this.builder.build(this.x, 0, this.z);
            int floorN = (int)this.n;
            if (this.j < floorN) {
               switch (this.i % 4) {
                  case 0:
                     this.z = this.z + SpiralIterator.this.step;
                     break;
                  case 1:
                     this.x = this.x + SpiralIterator.this.step;
                     break;
                  case 2:
                     this.z = this.z - SpiralIterator.this.step;
                     break;
                  case 3:
                     this.x = this.x - SpiralIterator.this.step;
               }

               this.j++;
               return returnValue;
            } else {
               this.j = 0;
               this.n += 0.5F;
               this.i++;
               return (T)this.next();
            }
         }
      };
   }

   @Override
   public Spliterator<T> spliterator() {
      return Spliterators.spliterator(
         this.iterator(),
         (long)((this.upperBound.getX() - this.lowerBound.getX()) / this.step + 1) * ((this.upperBound.getZ() - this.lowerBound.getZ()) / this.step + 1),
         16
      );
   }

   @FunctionalInterface
   public interface Builder<T extends Vec3i> {
      T build(int var1, int var2, int var3);
   }
}
