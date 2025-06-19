package sistem.kasir;

import java.text.NumberFormat;
import java.util.Locale;

public class currencyFormat {
    private static final NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.of("id", "ID"));

    public static String format(double amount) {
        return formatter.format(amount);
    }
}