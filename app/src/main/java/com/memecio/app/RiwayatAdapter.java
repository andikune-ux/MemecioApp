package com.memecio.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class RiwayatAdapter extends ArrayAdapter<String> {

    public interface OnDeleteListener {
        void onDelete(String url);
    }

    private Context context;
    private List<String> items;
    private OnDeleteListener deleteListener;

    public RiwayatAdapter(Context context, List<String> items, OnDeleteListener listener) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
        this.deleteListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_riwayat, parent, false);
        }

        final String url = items.get(position);
        TextView tvUrl = convertView.findViewById(R.id.tvRiwayatUrl);
        TextView btnSalin = convertView.findViewById(R.id.btnSalinUrl);
        TextView btnHapus = convertView.findViewById(R.id.btnHapusUrl);

        tvUrl.setText(url);

        btnSalin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("url", url);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "URL disalin", Toast.LENGTH_SHORT).show();
            }
        });

        btnHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new android.app.AlertDialog.Builder(context)
                    .setTitle("Hapus URL")
                    .setMessage("Hapus URL ini dari riwayat?")
                    .setNegativeButton("Batal", null)
                    .setPositiveButton("Hapus", (d, w) -> {
                        if (deleteListener != null) deleteListener.onDelete(url);
                    })
                    .show();
            }
        });

        return convertView;
    }
}
