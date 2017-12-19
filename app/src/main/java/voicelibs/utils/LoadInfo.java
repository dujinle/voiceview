package voicelibs.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2017/12/14.
 */

public class LoadInfo {
    private Context context;
    private SQliteManager sqm;
    private String path;
    public LoadInfo(Context context){
        this.context = context;
        sqm = new SQliteManager(this.context,"voice_db",null,1);
    }
    public Map<String,List> loadConfig(String path,String filename){
        Log.i("LoadInfo",path + filename);
        String filedir = path + "/" + filename;
        Map<String,List> wavinfos = new HashMap();
        this.path = path;
        try {
            FileReader reader = new FileReader(filedir);
            BufferedReader bufferedReader = new BufferedReader(reader);
            int start = -1;
            String wcase = "";

            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                if (line.startsWith("#")) {
                    continue;
                }
                String splits[] = line.split("\t");
                if (splits.length == 1) {
                    if (splits[0].endsWith("end")) {

                        start = -1;
                        continue;
                    } else if (start == -1) {
                        wcase = splits[0];
                        start = 1;
                        wavinfos.put(wcase, new ArrayList<LoadBase>());
                        continue;
                    }
                } else if (splits.length == 2) {
                    LoadBase lb = new LoadBase();
                    lb.setFilename(path + "/" +splits[0]);
                    lb.setContext(splits[1]);
                    lb.setWcase(wcase);
                    wavinfos.get(wcase).add(lb);
                }
            }
        }catch (IOException e){
            Log.e("LoadInfo",e.getMessage());
        }
        return wavinfos;
    }

    public int LoadWave(Map<String,List> maps){
        SQLiteDatabase db = sqm.getReadableDatabase();
        for (Map.Entry<String, List> entry : maps.entrySet()) {
            String wcase = entry.getKey();
            ArrayList<LoadBase> arrays = (ArrayList<LoadBase>) entry.getValue();
            for(int i = 0;i < arrays.size();i++){
                LoadBase lb = arrays.get(i);
                ContentValues cv = new ContentValues();
                //往ContentValues对象存放数据，键-值对模式
                cv.put("context", lb.getContext());
                cv.put("filename", lb.getFilename());
                cv.put("wcase",wcase);
                cv.put("read_num",0);
                //调用insert方法，将数据插入数据库
                db.insert("wav_table", null, cv);
                //关闭数据库
            }
            Cursor cursor = db.query("voice_table", new String[]{"dnum","did"}, "wcase=?", new String[]{wcase}, null, null, null);
            if(cursor.getCount() == 0){
                ContentValues cv = new ContentValues();
                //往ContentValues对象存放数据，键-值对模式
                cv.put("wcase", wcase);
                cv.put("dnum", arrays.size());
                cv.put("did",0);
                db.insert("voice_table",null,cv);
            }else{
                while(cursor.moveToNext()) {
                    ContentValues cv = new ContentValues();
                    cv.put("dnum", cursor.getInt(cursor.getColumnIndex("dnum")) + arrays.size());
                    String whereClause = "wcase=?";
                    String[] whereArgs = {wcase};
                    db.update("voice_table", cv, whereClause, whereArgs);
                }
            }
        }
        db.close();
        return 0;
    }
}
