package com.memecio.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import android.util.LruCache;

public class ThumbnailAdapter extends BaseAdapter {

    public interface OnThumbnailClickListener {
        void onThumbnailClick(MediaItem item);
    }

    public interface OnHideListener {
        void onHide(MediaItem item);
    }

    private OnHideListener hideListener;

    public void setOnHideListener(OnHideListener listener) {
        this.hideListener = listener;
    }

    private Context context;
    private List<MediaItem> items;
    private OnThumbnailClickListener listener;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean dataSaver = false;

    private static ExecutorService threadPool = Executors.newFixedThreadPool(6);
    private static ConcurrentHashMap<String, Future> futures = new ConcurrentHashMap<>();

    private static final int MAX_CACHE_KB = (int) (Runtime.getRuntime().maxMemory() / 1024 / 4);
    private static LruCache<String, Bitmap> memoryCache = new LruCache<String, Bitmap>(MAX_CACHE_KB) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            return bitmap.getByteCount() / 1024;
        }
    };

    public ThumbnailAdapter(Context context, List<MediaItem> items, OnThumbnailClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
        SharedPreferences prefs = context.getSharedPreferences("memecio_settings", Context.MODE_PRIVATE);
        this.dataSaver = prefs.getBoolean("data_saver", false);
    }

    @Override
    public int getCount() { return items.size(); }

    @Override
    public Object getItem(int position) { return items.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_thumbnail, parent, false);
        } else {
            view = convertView;
        }

        final ImageView imgThumb = view.findViewById(R.id.imgThumb);
        TextView playBadge = view.findViewById(R.id.tvPlayBadge);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvM3uBadge = view.findViewById(R.id.tvM3uBadge);
        final TextView btnHide = view.findViewById(R.id.btnHide);

        final MediaItem item = items.get(position);
        final String cacheKey = item.uri.toString() + "_" + item.title;

        // Set tinggi dinamis mengikuti lebar kolom (rasio 1.3x)
        try {
            int colWidth = parent.getWidth() / (parent instanceof android.widget.GridView
                ? ((android.widget.GridView) parent).getNumColumns() : 3);
            if (colWidth <= 0) colWidth = (int)(110 * context.getResources().getDisplayMetrics().density);
            int itemHeight = (int)(colWidth * 1.35f);
            android.view.ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp == null) {
                lp = new android.widget.AbsListView.LayoutParams(colWidth, itemHeight);
            } else {
                lp.width = colWidth;
                lp.height = itemHeight;
            }
            view.setLayoutParams(lp);
        } catch (Exception ignored) {}

        if (btnHide != null) {
            if (item.isLocal && item.type != MediaItem.TYPE_FOLDER) {
                btnHide.setVisibility(View.VISIBLE);
                btnHide.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (hideListener != null) hideListener.onHide(item);
                    }
                });
            } else {
                btnHide.setVisibility(View.GONE);
            }
        }

        imgThumb.setImageBitmap(null);
        imgThumb.setTag(cacheKey);

        playBadge.setVisibility(item.type == MediaItem.TYPE_VIDEO ? View.VISIBLE : View.GONE);
        if (tvM3uBadge != null) {
            tvM3uBadge.setVisibility(item.isM3u ? View.VISIBLE : View.GONE);
        }

        if (item.title != null && !item.title.isEmpty()) {
            tvTitle.setText(item.title);
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setSelected(true);
            tvTitle.setSingleLine(true);
            tvTitle.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE);
            tvTitle.setMarqueeRepeatLimit(-1);
            tvTitle.setHorizontallyScrolling(true);
        } else {
            tvTitle.setText("");
            tvTitle.setVisibility(View.GONE);
        }

        Bitmap cached = memoryCache.get(cacheKey);
        if (cached != null) {
            imgThumb.setImageBitmap(cached);
        } else if (item.isLocal) {
            loadLocalThumbnail(item, imgThumb, cacheKey);
        } else if (dataSaver) {
            imgThumb.setImageResource(android.R.drawable.ic_menu_gallery);
        } else {
            if (futures.containsKey(cacheKey)) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        if (listener != null) listener.onThumbnailClick(item);
                    }
                });
                return view;
            }
            final String urlToLoad;
            if (item.type == MediaItem.TYPE_IMAGE) {
                urlToLoad = item.uri.toString();
            } else if (item.thumbUrl != null && !item.thumbUrl.isEmpty()) {
                urlToLoad = item.thumbUrl;
            } else {
                urlToLoad = null;
            }
            if (urlToLoad != null) {
                Future future = threadPool.submit(new Runnable() {
                    @Override public void run() {
                        Bitmap bmp = loadRemote(urlToLoad);
                        if (bmp != null) memoryCache.put(cacheKey, bmp);
                        final Bitmap fb = bmp;
                        mainHandler.post(new Runnable() {
                            @Override public void run() {
                                futures.remove(cacheKey);
                                if (cacheKey.equals(imgThumb.getTag()) && fb != null) {
                                    imgThumb.setImageBitmap(fb);
                                }
                            }
                        });
                    }
                });
                futures.put(cacheKey, future);
            }
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (listener != null) listener.onThumbnailClick(item);
            }
        });

        return view;
    }

    private void loadLocalThumbnail(final MediaItem item, final ImageView imgThumb, final String cacheKey) {
        if (futures.containsKey(cacheKey)) return;
        Future future = threadPool.submit(new Runnable() {
            @Override public void run() {
                Bitmap bitmap = null;
                try {
                    long id = Long.parseLong(item.uri.getLastPathSegment());
                    if (item.type == MediaItem.TYPE_VIDEO) {
                        bitmap = MediaStore.Video.Thumbnails.getThumbnail(
                                context.getContentResolver(), id,
                                MediaStore.Video.Thumbnails.MINI_KIND, null);
                    } else if (item.type == MediaItem.TYPE_IMAGE) {
                        bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                                context.getContentResolver(), id,
                                MediaStore.Images.Thumbnails.MINI_KIND, null);
                    }
                } catch (Exception ignored) {}
                if (bitmap != null) memoryCache.put(cacheKey, bitmap);
                final Bitmap fb = bitmap;
                mainHandler.post(new Runnable() {
                    @Override public void run() {
                        futures.remove(cacheKey);
                        if (cacheKey.equals(imgThumb.getTag()) && fb != null) {
                            imgThumb.setImageBitmap(fb);
                        } else if (item.type == 4) {
                            imgThumb.setImageResource(android.R.drawable.ic_media_play);
                        }
                    }
                });
            }
        });
        futures.put(cacheKey, future);
    }

    private Bitmap loadRemote(String urlStr) {
        try {
            InputStream inputStream;
            if (urlStr.startsWith("http://") || urlStr.startsWith("https://")) {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                inputStream = conn.getInputStream();
            } else {
                inputStream = context.getContentResolver().openInputStream(android.net.Uri.parse(urlStr));
            }
            byte[] bytes = readAllBytes(inputStream);
            if (inputStream != null) inputStream.close();
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 2;
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] readAllBytes(InputStream in) throws Exception {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}
