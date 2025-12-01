package com.chamei.pim4.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * Helpers de UI basicos (toast, esconder teclado).
 */
public final class Ui {
    private Ui() {}

    public static void toast(Context ctx, String msg) {
        // Exibe toast curto padrao na app
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view == null) return;
        // Pede para o InputMethodManager esconder o teclado aberto
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
