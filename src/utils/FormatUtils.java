package utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FormatUtils {

    private static final DecimalFormat AMOUNT_FORMAT;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator(',');
        AMOUNT_FORMAT = new DecimalFormat("#,##0.00", symbols);
    }

    public static String formatAmount(BigDecimal amount) {
        if (amount == null) return "0,00 Ar";
        return AMOUNT_FORMAT.format(amount) + " Ar";
    }

    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMAT);
    }

    public static String formatPercentage(double value) {
        return String.format("%.1f%%", value);
    }

    public static String getMonthName(int month) {
        String[] months = {"Janvier","Février","Mars","Avril","Mai","Juin",
                           "Juillet","Août","Septembre","Octobre","Novembre","Décembre"};
        if (month < 1 || month > 12) return "";
        return months[month - 1];
    }
}
