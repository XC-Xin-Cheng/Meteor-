package com.seedfinding.mccore.version;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VersionMap<V> extends AbstractMap<MCVersion, V> {
   protected List<VersionMap.Entry<V>> entries = new ArrayList<>();

   @Override
   public Set<java.util.Map.Entry<MCVersion, V>> entrySet() {
      return new LinkedHashSet<>(this.entries);
   }

   public VersionMap<V> add(MCVersion key, V value) {
      this.put(key, value);
      return this;
   }

   public V put(MCVersion key, V value) {
      for (VersionMap.Entry<V> e : this.entries) {
         if (e.getKey() == key) {
            return e.setValue(value);
         }
      }

      this.entries.add(new VersionMap.Entry<>(key, value));
      this.sort();
      return null;
   }

   public <E, K> Map<E, K> getMapUntil(MCVersion version) {
      Map<E, K> value = null;
      ArrayList<VersionMap.Entry<V>> entries = new ArrayList<>(this.entries);
      entries.sort((a, b) -> b.getKey().compareTo(a.getKey()));

      for (VersionMap.Entry<V> e : entries) {
         if (!e.getKey().isNewerThan(version)) {
            if (value == null) {
               value = (Map<E, K>)e.getValue();
            } else {
               for (java.util.Map.Entry<E, K> entry : ((Map)e.getValue()).entrySet()) {
                  value.put(entry.getKey(), entry.getValue());
               }
            }
         }
      }

      return value;
   }

   public V getAsOf(MCVersion version) {
      V value = null;

      for (VersionMap.Entry<V> e : this.entries) {
         if (e.getKey().isNewerThan(version)) {
            break;
         }

         value = e.getValue();
      }

      return value;
   }

   public V getLatest() {
      return this.isEmpty() ? null : this.entries.get(this.entries.size() - 1).getValue();
   }

   public MCVersion getLatestVersion() {
      return this.isEmpty() ? null : this.entries.get(this.entries.size() - 1).getKey();
   }

   public V getOldest() {
      return this.isEmpty() ? null : this.entries.get(0).getValue();
   }

   public MCVersion getOldestVersion() {
      return this.isEmpty() ? null : this.entries.get(0).getKey();
   }

   protected void sort() {
      this.entries.sort((o1, o2) -> o1.getKey().isOlderThan(o2.getKey()) ? -1 : (o1.getKey().isEqualTo(o2.getKey()) ? 0 : 1));
   }

   public static class Entry<V> implements java.util.Map.Entry<MCVersion, V> {
      private final MCVersion key;
      private V value;

      public Entry(MCVersion key, V value) {
         this.key = key;
         this.value = value;
      }

      public MCVersion getKey() {
         return this.key;
      }

      @Override
      public V getValue() {
         return this.value;
      }

      @Override
      public V setValue(V value) {
         V old = this.getValue();
         this.value = value;
         return old;
      }

      @Override
      public boolean equals(Object o) {
         return false;
      }

      @Override
      public int hashCode() {
         return this.key.hashCode() * 31 + this.value.hashCode();
      }
   }
}
