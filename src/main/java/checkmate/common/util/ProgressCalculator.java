package checkmate.common.util;

import static java.math.BigDecimal.ROUND_HALF_UP;

import java.math.BigDecimal;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProgressCalculator {

    public static double calculate(int progressedCount, int totalCount) {
        if (hasZero(progressedCount, totalCount)) {
            return 0;
        }
        return divide(progressedCount, totalCount).doubleValue() * 100;
    }

    private static BigDecimal divide(int progressedCount, int totalCount) {
        return new BigDecimal(progressedCount).divide(new BigDecimal(totalCount), 3, ROUND_HALF_UP);
    }

    private static boolean hasZero(int progressedCount, int totalCount) {
        return progressedCount <= 0 || totalCount <= 0;
    }

}
