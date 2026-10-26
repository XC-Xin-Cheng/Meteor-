package com.seedfinding.mcmath.lattice;

import com.seedfinding.mcmath.arithmetic.Rational;
import com.seedfinding.mcmath.component.matrix.QMatrix;
import com.seedfinding.mcmath.component.vector.QVector;

public final class LagrangeGauss {
   public static boolean supports(QMatrix basis) {
      return basis.getRowCount() == 2 && basis.getColumnCount() == 2;
   }

   public static QMatrix reduce(QMatrix basis) {
      return reduceAndSet(basis.copy());
   }

   public static QMatrix reduceAndSet(QMatrix basis) {
      QVector v1 = basis.getRow(0);
      QVector v2 = basis.getRow(1);
      QVector minVec;
      QVector maxVec;
      if (v1.magnitudeSq().compareTo(v2.magnitudeSq()) <= 0) {
         minVec = v1;
         maxVec = v2;
      } else {
         minVec = v2;
         maxVec = v1;
      }

      Rational minNorm;
      Rational maxNorm;
      do {
         minNorm = minVec.magnitudeSq();
         maxVec.subtractAndSet(minVec.scale(minVec.dot(maxVec).divide(minNorm).round()));
         maxNorm = maxVec.magnitudeSq();
         QVector temp = minVec;
         minVec = maxVec;
         maxVec = temp;
      } while (minNorm.compareTo(maxNorm) > 0);

      return basis;
   }
}
