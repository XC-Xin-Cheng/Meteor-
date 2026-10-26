package org.fusesource.hawtjni.runtime;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

public class NativeStats {
   private final HashMap<NativeStats.StatsInterface, ArrayList<NativeStats.NativeFunction>> snapshot;

   public NativeStats(NativeStats.StatsInterface... classes) {
      this(Arrays.asList(classes));
   }

   public NativeStats(Collection<NativeStats.StatsInterface> classes) {
      this(snapshot(classes));
   }

   private NativeStats(HashMap<NativeStats.StatsInterface, ArrayList<NativeStats.NativeFunction>> snapshot) {
      this.snapshot = snapshot;
   }

   public void reset() {
      for (ArrayList<NativeStats.NativeFunction> functions : this.snapshot.values()) {
         for (NativeStats.NativeFunction function : functions) {
            function.reset();
         }
      }
   }

   public void update() {
      for (Entry<NativeStats.StatsInterface, ArrayList<NativeStats.NativeFunction>> entry : this.snapshot.entrySet()) {
         NativeStats.StatsInterface si = entry.getKey();

         for (NativeStats.NativeFunction function : entry.getValue()) {
            function.setCounter(si.functionCounter(function.getOrdinal()));
         }
      }
   }

   public NativeStats snapshot() {
      NativeStats copy = this.copy();
      copy.update();
      return copy;
   }

   public NativeStats copy() {
      HashMap<NativeStats.StatsInterface, ArrayList<NativeStats.NativeFunction>> rc = new HashMap<>(this.snapshot.size() * 2);

      for (Entry<NativeStats.StatsInterface, ArrayList<NativeStats.NativeFunction>> entry : this.snapshot.entrySet()) {
         ArrayList<NativeStats.NativeFunction> list = new ArrayList<>(entry.getValue().size());

         for (NativeStats.NativeFunction function : entry.getValue()) {
            list.add(function.copy());
         }

         rc.put(entry.getKey(), list);
      }

      return new NativeStats(rc);
   }

   public NativeStats diff() {
      HashMap<NativeStats.StatsInterface, ArrayList<NativeStats.NativeFunction>> rc = new HashMap<>(this.snapshot.size() * 2);

      for (Entry<NativeStats.StatsInterface, ArrayList<NativeStats.NativeFunction>> entry : this.snapshot.entrySet()) {
         NativeStats.StatsInterface si = entry.getKey();
         ArrayList<NativeStats.NativeFunction> list = new ArrayList<>(entry.getValue().size());

         for (NativeStats.NativeFunction original : entry.getValue()) {
            NativeStats.NativeFunction copy = original.copy();
            copy.setCounter(si.functionCounter(copy.getOrdinal()));
            copy.subtract(original);
            list.add(copy);
         }

         rc.put(si, list);
      }

      return new NativeStats(rc);
   }

   public void dump(PrintStream ps) {
      boolean firstSI = true;

      for (Entry<NativeStats.StatsInterface, ArrayList<NativeStats.NativeFunction>> entry : this.snapshot.entrySet()) {
         NativeStats.StatsInterface si = entry.getKey();
         ArrayList<NativeStats.NativeFunction> funcs = entry.getValue();
         int total = 0;

         for (NativeStats.NativeFunction func : funcs) {
            total += func.getCounter();
         }

         if (!firstSI) {
            ps.print(", ");
         }

         firstSI = false;
         ps.print("[");
         if (total > 0) {
            ps.println("{ ");
            ps.println("  \"class\": \"" + si.getNativeClass() + "\",");
            ps.println("  \"total\": " + total + ", ");
            ps.print("  \"functions\": {");
            boolean firstFunc = true;

            for (NativeStats.NativeFunction func : funcs) {
               if (func.getCounter() > 0) {
                  if (!firstFunc) {
                     ps.print(",");
                  }

                  firstFunc = false;
                  ps.println();
                  ps.print("    \"" + func.getName() + "\": " + func.getCounter());
               }
            }

            ps.println();
            ps.println("  }");
            ps.print("}");
         }

         ps.print("]");
      }
   }

   private static HashMap<NativeStats.StatsInterface, ArrayList<NativeStats.NativeFunction>> snapshot(Collection<NativeStats.StatsInterface> classes) {
      HashMap<NativeStats.StatsInterface, ArrayList<NativeStats.NativeFunction>> rc = new HashMap<>();

      for (NativeStats.StatsInterface sc : classes) {
         int count = sc.functionCount();
         ArrayList<NativeStats.NativeFunction> functions = new ArrayList<>(count);

         for (int i = 0; i < count; i++) {
            String name = sc.functionName(i);
            functions.add(new NativeStats.NativeFunction(i, name, 0));
         }

         Collections.sort(functions);
         rc.put(sc, functions);
      }

      return rc;
   }

   public static class NativeFunction implements Comparable<NativeStats.NativeFunction> {
      private final int ordinal;
      private final String name;
      private int counter;

      public NativeFunction(int ordinal, String name, int callCount) {
         this.ordinal = ordinal;
         this.name = name;
         this.counter = callCount;
      }

      void subtract(NativeStats.NativeFunction func) {
         this.counter = this.counter - func.counter;
      }

      public int getCounter() {
         return this.counter;
      }

      public void setCounter(int counter) {
         this.counter = counter;
      }

      public String getName() {
         return this.name;
      }

      public int getOrdinal() {
         return this.ordinal;
      }

      public int compareTo(NativeStats.NativeFunction func) {
         return func.counter - this.counter;
      }

      public void reset() {
         this.counter = 0;
      }

      public NativeStats.NativeFunction copy() {
         return new NativeStats.NativeFunction(this.ordinal, this.name, this.counter);
      }
   }

   public interface StatsInterface {
      String getNativeClass();

      int functionCount();

      String functionName(int var1);

      int functionCounter(int var1);
   }
}
