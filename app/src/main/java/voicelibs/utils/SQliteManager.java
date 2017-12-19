package voicelibs.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by yingy on 2017/12/14.
 */

public class SQliteManager extends SQLiteOpenHelper {

    public SQliteManager(Context context, String name, SQLiteDatabase.CursorFactory factory,int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建表SQL语句
        String voice ="create table voice_table(" +
                "id integer primary key autoincrement," +
                "dnum INTEGER," +
                "wcase text," +
                "did integer)";
        //执行SQL语句
        db.execSQL(voice);
        String vtables ="create table wav_table(" +
                "id integer primary key autoincrement," +
                "context text," +
                "filename text," +
                "wcase text," +
                "read_num integer)";
        //执行SQL语句
        db.execSQL(vtables);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}