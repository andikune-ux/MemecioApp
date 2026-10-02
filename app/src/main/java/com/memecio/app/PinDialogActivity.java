package com.memecio.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PinDialogActivity extends Activity {

    public static final String EXTRA_TARGET = "target";
    public static final String TARGET_SERVER = "server";
    public static final String TARGET_RIWAYAT = "riwayat";

    private static final String PIN_CODE = "808080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_dialog);

        final EditText etPin = findViewById(R.id.etPin);
        Button btnOk = findViewById(R.id.btnPinOk);
        Button btnCancel = findViewById(R.id.btnPinCancel);

        final String target = getIntent().getStringExtra(EXTRA_TARGET);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = etPin.getText().toString().trim();
                if (input.equals(PIN_CODE)) {
                    if (TARGET_SERVER.equals(target)) {
                        startActivity(new android.content.Intent(PinDialogActivity.this, ServerSourceActivity.class));
                    } else if (TARGET_RIWAYAT.equals(target)) {
                        startActivity(new android.content.Intent(PinDialogActivity.this, RiwayatActivity.class));
                    }
                    finish();
                } else {
                    Toast.makeText(PinDialogActivity.this, "PIN salah", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

