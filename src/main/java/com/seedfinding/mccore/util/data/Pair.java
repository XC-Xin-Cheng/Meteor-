package com.seedfinding.mccore.util.data;

import java.util.Objects;

public final class Pair<A, B> {
   private final A a;
   private final B b;

   public Pair(A a, B b) {
      this.a = a;
      this.b = b;
   }

   public Pair(Pair<? extends A, ? extends B> other) {
      this((A)other.a, (B)other.b);
   }

   public A getFirst() {
      return this.a;
   }

   public B getSecond() {
      return this.b;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Pair)) {
         return false;
      } else {
         Pair<?, ?> pair = (Pair<?, ?>)o;
         return Objects.equals(this.a, pair.a) && Objects.equals(this.b, pair.b);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.a, this.b);
   }

   @Override
   public String toString() {
      return "(" + this.a + ", " + this.b + ")";
   }
}
