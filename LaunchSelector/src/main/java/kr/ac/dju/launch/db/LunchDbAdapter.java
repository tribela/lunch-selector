package kr.ac.dju.launch.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kjwon15 on 2014. 5. 4..
 */
public class LunchDbAdapter {

    private final static int DB_VERSION = 1;
    private final static String DB_NAME = "lunch_preset.db";

    private final static String TABLE_PRESETS = "presets";
    private final static String TABLE_ELEMENTS = "presets";
    private final static String KEY_ID = "rowid";
    private final static String KEY_NAME = "name";
    private final static String KEY_PRESET_ID = "preset_id";
    private final static String KEY_CONTENT = "content";

    private Context context;
    private DbHelper dbHelper;
    private SQLiteDatabase db;


    public LunchDbAdapter(Context context) {
        this.context = context;
    }

    public LunchDbAdapter open() {
        dbHelper = new DbHelper(context);
        db = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public long createPreset(String presetName) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, presetName);
        return db.insert(TABLE_PRESETS, null, values);
    }

    public long createElement(long presetId, String content) {
        ContentValues values = new ContentValues();
        values.put(KEY_PRESET_ID, presetId);
        values.put(KEY_CONTENT, content);
        return db.insert(TABLE_ELEMENTS, null, values);
    }

    public boolean deletePreset(long rowid) {
        db.delete(TABLE_ELEMENTS, KEY_PRESET_ID + "=?",
                new String[]{String.valueOf(rowid)}
        );

        int result = db.delete(TABLE_PRESETS, KEY_ID + "=?",
                new String[]{String.valueOf(rowid)}
        );

        return result > 0;
    }

    public boolean deleteElement(long rowid) {
        int result = db.delete(TABLE_ELEMENTS, KEY_ID + "=?",
                new String[]{String.valueOf(rowid)}
        );

        return result > 0;
    }

    public List<Preset> fetchAllPresets() {
        List<Preset> list = new ArrayList<Preset>();
        Cursor cursor = db.query(TABLE_PRESETS, new String[]{KEY_ID, KEY_NAME},
                null, null, null, null, null);

        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            long rowid = cursor.getLong(cursor.getColumnIndex(KEY_ID));
            String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
            List<Element> elements = fetchAllElements(rowid);
            Preset preset = new Preset(rowid, name, elements);
            list.add(preset);
        }

        return list;
    }

    public List<Element> fetchAllElements(long rowId) {
        List<Element> list = new ArrayList<Element>();
        Cursor cursor = db.query(TABLE_ELEMENTS, new String[]{KEY_ID, KEY_PRESET_ID, KEY_CONTENT},
                null, null, null, null, null);

        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            long rowid = cursor.getLong(cursor.getColumnIndex(KEY_ID));
            long presetId = cursor.getLong(cursor.getColumnIndex(KEY_PRESET_ID));
            String content = cursor.getString(cursor.getColumnIndex(KEY_CONTENT));
            Element element = new Element(rowid, presetId, content);

            list.add(element);
        }

        return list;
    }


    private class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table ? (" +
                    "? varchar not null" +
                    ");" +
                    new String[]{TABLE_PRESETS,
                            KEY_NAME}
            );

            db.execSQL("create table ?(" +
                    "? integer not null," +
                    "? varchar not null," +
                    "foreign key (?) references ? (?)" +
                    ");",
                    new String[]{TABLE_ELEMENTS,
                            KEY_PRESET_ID, KEY_CONTENT,
                            KEY_PRESET_ID, TABLE_PRESETS, KEY_ID}
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}