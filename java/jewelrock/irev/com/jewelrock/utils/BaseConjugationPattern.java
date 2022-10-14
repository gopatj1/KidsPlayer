package jewelrock.irev.com.jewelrock.utils;

public class BaseConjugationPattern {
    public static String conjugate(int count, String singleTitle, String singleGenitive, String pluralGenitive) {
        if (count > 10 && count < 20) return pluralGenitive;
        if (String.valueOf(count).length() >= 3) {
            return conjugate(count - count / 100 * 100, singleTitle, singleGenitive, pluralGenitive);
        }
        int remainder = count % 10;
        if (remainder == 1) {
            return singleTitle;
        } else if ((remainder >= 2) && (remainder <= 4)) {
            return singleGenitive;
        } else {
            return pluralGenitive;
        }
    }
}
