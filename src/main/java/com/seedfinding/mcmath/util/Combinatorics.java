package com.seedfinding.mcmath.util;

import java.math.BigInteger;
import java.util.function.Predicate;

public final class Combinatorics {
   public static final int MAX_LONG_FACTORIAL = 20;
   private static final long[] FACTORIAL = new long[21];

   public static long getFactorial(int n) {
      return FACTORIAL[n];
   }

   public static BigInteger getBigFactorial(int n) {
      if (n <= 20) {
         return BigInteger.valueOf(getFactorial(n));
      } else {
         BigInteger result = BigInteger.valueOf(getFactorial(20));

         for (int i = 20; i < n; i++) {
            result = result.multiply(BigInteger.valueOf(i + 1));
         }

         return result;
      }
   }

   public static long getPermutations(int n, int r) {
      return getFactorial(n) / getFactorial(n - r);
   }

   public static BigInteger getBigPermutations(int n, int r) {
      return getBigFactorial(n).divide(getBigFactorial(n - r));
   }

   public static long getCombinations(int n, int r) {
      return getPermutations(n, r) / getFactorial(r);
   }

   public static BigInteger getBigCombinations(int n, int r) {
      return getBigPermutations(n, r).divide(getBigFactorial(r));
   }

   public static void permute(int n, int r, Predicate<int[]> shouldContinue) {
      if (n > 20) {
         bigPermute(n, r, shouldContinue);
      } else {
         smallPermute(n, r, shouldContinue);
      }
   }

   private static void smallPermute(int n, int r, Predicate<int[]> shouldContinue) {
      long max = getFactorial(n);
      long increment = getFactorial(n - r);

      for (long perm = 0L; perm < max; perm += increment) {
         long permCopy = perm;
         int[] indices = new int[n];

         for (int i = 0; i < n; i++) {
            indices[i] = (int)(permCopy / getFactorial(n - 1 - i));
            permCopy -= indices[i] * getFactorial(n - 1 - i);
         }

         if (!acceptPermutation(n, r, indices, shouldContinue)) {
            return;
         }
      }
   }

   private static void bigPermute(int n, int r, Predicate<int[]> shouldContinue) {
      BigInteger max = getBigFactorial(n);
      BigInteger increment = getBigFactorial(n - r);

      for (BigInteger perm = BigInteger.ZERO; perm.compareTo(max) < 0; perm = perm.add(increment)) {
         BigInteger permCopy = perm;
         int[] indices = new int[n];

         for (int i = 0; i < n; i++) {
            BigInteger f = getBigFactorial(n - 1 - i);
            indices[i] = permCopy.divide(f).intValue();
            permCopy = permCopy.subtract(f.multiply(BigInteger.valueOf(indices[i])));
         }

         if (!acceptPermutation(n, r, indices, shouldContinue)) {
            return;
         }
      }
   }

   private static boolean acceptPermutation(int n, int r, int[] indices, Predicate<int[]> shouldContinue) {
      int[] permutation = new int[n];

      for (int i = 0; i < r; i++) {
         int wantedIndex = indices[i];
         int currentIndex = 0;

         for (int j = 0; j < permutation.length; j++) {
            if (permutation[j] == 0 && currentIndex++ == wantedIndex) {
               currentIndex = j;
               break;
            }
         }

         permutation[currentIndex] = i + 1;
      }

      return shouldContinue.test(permutation);
   }

   public static void combine(int n, int r, Predicate<int[]> shouldContinue) {
      int[] combination = new int[r];
      int i = 0;

      while (i < r) {
         combination[i] = i++;
      }

      while (combination[r - 1] < n) {
         if (!shouldContinue.test(combination)) {
            return;
         }

         i = r - 1;

         while (i != 0 && combination[i] == n - r + i) {
            i--;
         }

         combination[i]++;

         for (int ix = i + 1; ix < r; ix++) {
            combination[ix] = combination[ix - 1] + 1;
         }
      }
   }

   static {
      FACTORIAL[0] = 1L;

      for (int i = 1; i < FACTORIAL.length; i++) {
         FACTORIAL[i] = FACTORIAL[i - 1] * i;
      }
   }
}
