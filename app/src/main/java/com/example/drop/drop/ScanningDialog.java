package com.example.drop.drop;

import android.app.ProgressDialog;
import android.content.Context;

public class ScanningDialog extends ProgressDialog {

    public ScanningDialog(Context context) {
        super(context);

        setMessage(context.getString(R.string.scanning));
        setCancelable(false);
    }
}
