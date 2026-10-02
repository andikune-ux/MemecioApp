package com.memecio.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class CustomPlaylistActivity extends Activity {

    private ListView listView;
    private TextView tvEmpty;
    private Button btnTambah;
    private List<CustomPlaylistStore.Playlist> playlists;
    private ArrayAdapter<CustomPlaylistStore.Playlist> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_playlist);

        TextView btnBack = findViewById(R.id.btnBackCustomPlaylist);
        tvEmpty = findViewById(R.id.tvCustomPlaylistEmpty);
        listView = findViewById(R.id.listCustomPlaylist);
        btnTambah = findViewById(R.id.btnTambahPlaylist);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnTambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogTambah();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CustomPlaylistStore.Playlist pl = playlists.get(position);
                Intent intent = new Intent(CustomPlaylistActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("open_custom_playlist", position);
                startActivity(intent);
                finish();
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                confirmHapus(position);
                return true;
            }
        });

        loadData();
    }

    private void loadData() {
        playlists = CustomPlaylistStore.getAll(this);
        adapter = new ArrayAdapter<CustomPlaylistStore.Playlist>(this, 0, playlists) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = LayoutInflater.from(CustomPlaylistActivity.this)
                            .inflate(android.R.layout.simple_list_item_2, parent, false);
                }
                CustomPlaylistStore.Playlist pl = getItem(position);
                TextView tv1 = view.findViewById(android.R.id.text1);
                TextView tv2 = view.findViewById(android.R.id.text2);
                tv1.setText(pl.name);
                tv1.setTextColor(0xFF1C1C1E);
                tv2.setText(pl.items.size() + " item");
                tv2.setTextColor(0xFF8A8A8E);
                return view;
            }
        };
        listView.setAdapter(adapter);

        if (playlists.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    private void showDialogTambah() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_input_nama);

        final EditText etNama = dialog.findViewById(R.id.etNamaPlaylist);
        Button btnBatal = dialog.findViewById(R.id.btnBatalNama);
        Button btnSimpan = dialog.findViewById(R.id.btnSimpanNama);

        btnBatal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nama = etNama.getText().toString().trim();
                if (nama.isEmpty()) {
                    Toast.makeText(CustomPlaylistActivity.this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }
                CustomPlaylistStore.tambahPlaylist(CustomPlaylistActivity.this, nama);
                dialog.dismiss();
                loadData();
                Toast.makeText(CustomPlaylistActivity.this, "Playlist dibuat", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void confirmHapus(final int position) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Playlist")
                .setMessage("Hapus \"" + playlists.get(position).name + "\"?")
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CustomPlaylistStore.hapusPlaylist(CustomPlaylistActivity.this, position);
                        loadData();
                    }
                })
                .show();
    }
}
