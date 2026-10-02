package com.memecio.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class SecretCodesActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret_codes);

        findViewById(R.id.btnBackSecret).setOnClickListener(v -> finish());

        ListView list = findViewById(R.id.listSecretCodes);
        final List<SecretCodeRegistry.Code> codes = SecretCodeRegistry.getVisible();

        list.setAdapter(new BaseAdapter() {
            @Override public int getCount() { return codes.size(); }
            @Override public Object getItem(int pos) { return codes.get(pos); }
            @Override public long getItemId(int pos) { return pos; }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    v = LayoutInflater.from(SecretCodesActivity.this)
                        .inflate(R.layout.item_secret_code, parent, false);
                }
                SecretCodeRegistry.Code c = codes.get(position);
                ((TextView) v.findViewById(R.id.tvSecretCode)).setText(c.code);
                ((TextView) v.findViewById(R.id.tvSecretTitle)).setText(c.title);
                ((TextView) v.findViewById(R.id.tvSecretDesc)).setText(c.description);
                return v;
            }
        });
    }
}
