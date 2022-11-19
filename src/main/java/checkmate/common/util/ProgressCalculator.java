package checkmate.common.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

import static java.math.BigDecimal.ROUND_HALF_UP;

@UtilityClass
public class ProgressCalculator {
    public static double calculate(int progressedCount, int totalCount) {
        BigDecimal progressed = new BigDecimal(progressedCount);
        BigDecimal total = new BigDecimal(totalCount);
        if(existZero(progressed, total)) return 0;
        else return getPercentage(progressed, total);
    }

    private static boolean existZero(BigDecimal progressed, BigDecimal total) {
        return total.compareTo(BigDecimal.ZERO) <= 0 || progressed.compareTo(BigDecimal.ZERO) <= 0;
    }

    private static double getPercentage(BigDecimal progressedCount, BigDecimal totalCount) {
        return progressedCount.divide(totalCount, 3, ROUND_HALF_UP).doubleValue() * 100;
    }
}
