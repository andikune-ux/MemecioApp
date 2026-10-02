package com.memecio.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrashLogActivity extends Activity {

    private static class CrashEntry {
        String title;
        String body;
    }

    private List<CrashEntry> entries = new ArrayList<>();
    private ArrayAdapter<CrashEntry> adapter;
    private ListView listView;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash_log);

        TextView btnBack = findViewById(R.id.btnBackCrashLog);
        TextView btnHapus = findViewById(R.id.btnHapusRiwayatCrash);
        listView = findViewById(R.id.listCrashLog);
        tvEmpty = findViewById(R.id.tvCrashEmpty);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File f = new File(getFilesDir(), "memecio_crash.txt");
                if (f.exists()) f.delete();
                loadEntries();
            }
        });

        adapter = new ArrayAdapter<CrashEntry>(this, 0, entries) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = LayoutInflater.from(getContext()).inflate(R.layout.item_crash_entry, parent, false);
                }
                CrashEntry entry = getItem(position);
                TextView tvTitle = view.findViewById(R.id.tvCrashEntryTitle);
                tvTitle.setText(entry.title);
                return view;
            }
        };
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CrashEntry entry = entries.get(position);
                Intent intent = new Intent(CrashLogActivity.this, CrashLogDetailActivity.class);
                intent.putExtra("crash_title", entry.title);
                intent.putExtra("crash_body", entry.body);
                startActivity(intent);
            }
        });

        loadEntries();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEntries();
    }

    private void loadEntries() {
        entries.clear();

        File file = new File(getFilesDir(), "memecio_crash.txt");
        if (file.exists()) {
            try {
                StringBuilder sb = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                reader.close();

                String content = sb.toString();
                Pattern pattern = Pattern.compile("=== .*?(?:CRASH at |crash )?([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}).*?===\\n");
                Matcher matcher = pattern.matcher(content);

                List<int[]> markers = new ArrayList<>();
                List<String> titles = new ArrayList<>();
                while (matcher.find()) {
                    markers.add(new int[]{matcher.start(), matcher.end()});
                    titles.add(matcher.group(1));
                }

                for (int i = 0; i < markers.size(); i++) {
                    int bodyStart = markers.get(i)[1];
                    int bodyEnd = (i + 1 < markers.size()) ? markers.get(i + 1)[0] : content.length();
                    String body = content.substring(bodyStart, bodyEnd).trim();

                    CrashEntry entry = new CrashEntry();
                    entry.title = titles.get(i);
                    entry.body = body;
                    entries.add(0, entry);
                }
            } catch (Exception ignored) { }
        }

        adapter.notifyDataSetChanged();

        if (entries.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }
}

