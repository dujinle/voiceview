package voicepro.application;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.RuntimePermissions;
import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.data.ExFilePickerResult;
import voicelibs.utils.LoadInfo;
import voicelibs.utils.SQliteManager;
import voicelibs.utils.U;

/**
 * Created by yingy on 2017/12/14.
 */

public class MainListActivity extends Activity {
    private static final int EX_FILE_PICKER_RESULT = 0;
    @BindView(R.id.list) ListView list;
    @BindView(R.id.load_btn) TextView load_btn;
    private SQliteManager sqm;
    private List<Map<String, Object>> mData;
    private ExFilePicker exFilePicker;
    private LoadInfo loadinfo;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        exFilePicker = new ExFilePicker();
        ButterKnife.bind(this);

        load_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exFilePicker.start(MainListActivity.this, EX_FILE_PICKER_RESULT);
            }
        });
        sqm = new SQliteManager(MainListActivity.this,"voice_db",null,1);
        initListView();

    }

    public class DownloadTask extends AsyncTask<String, String,Integer> {
        //后面尖括号内分别是参数（线程休息时间），进度(publishProgress用到)，返回值 类型

        private Context mContext=null;
        private ProgressBar mProgressBar=null;
        private TextView mTextView=null;
        public DownloadTask(Context context,ProgressBar pb,TextView tv){
            this.mContext=context;
            this.mProgressBar=pb;
            this.mTextView=tv;
        }
        /*
         * 第一个执行的方法
         * 执行时机：在执行实际的后台操作前，被UI 线程调用
         * 作用：可以在该方法中做一些准备工作，如在界面上显示一个进度条，或者一些控件的实例化，这个方法可以不用实现。
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            Log.d("sn", "00000");
            super.onPreExecute();
        }

        /*
         * 执行时机：在onPreExecute 方法执行后马上执行，该方法运行在后台线程中
         * 作用：主要负责执行那些很耗时的后台处理工作。可以调用 publishProgress方法来更新实时的任务进度。该方法是抽象方法，子类必须实现。
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub
            Log.d("sn", "1111111");
            loadinfo = new LoadInfo(MainListActivity.this);
            Map<String,List> maps = loadinfo.loadConfig(params[0],params[1]);
            return loadinfo.LoadWave(maps);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if(integer == 0){
                initListView();
            }
            super.onPostExecute(integer);
        }

        /*
         * 执行时机：这个函数在doInBackground调用publishProgress时被调用后，UI 线程将调用这个方法.虽然此方法只有一个参数,但此参数是一个数组，可以用values[i]来调用
         * 作用：在界面上展示任务的进展情况，例如通过一个进度条进行展示。此实例中，该方法会被执行100次
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         */
        @Override
        protected void onProgressUpdate(String... values) {
            // TODO Auto-generated method stub
            Log.d("Main", values[0]);
            super.onProgressUpdate(values);
        }

    }
    public void initListView(){
        mData = getData();
        MyAdapter adapter = new MyAdapter(this);
        list.setAdapter(adapter);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EX_FILE_PICKER_RESULT) {
            ExFilePickerResult result = ExFilePickerResult.getFromIntent(data);
            if (result != null && result.getCount() == 1) {
                DownloadTask task = new DownloadTask(MainListActivity.this,null,null);
                task.execute(result.getPath(),result.getNames().get(0));
            }
        }
    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteDatabase db = sqm.getReadableDatabase();
        Cursor cursor = db.query("voice_table", new String[]{"id","dnum","wcase","did"}, null, null, null, null, null);
        while(cursor.moveToNext()){
            String title = cursor.getString(cursor.getColumnIndex("wcase"));
            int dnum = cursor.getInt(cursor.getColumnIndex("dnum"));
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("title", title);
            map.put("info", String.valueOf(dnum) + "个");
            list.add(map);
        }
        //关闭数据库
        db.close();
        return list;
    }

    public class MyAdapter extends BaseAdapter {

        private LayoutInflater mInflater;


        public MyAdapter(Context context){
            this.mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mData.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        public final class ViewHolder{
            public TextView title;
            public TextView info;
            public TextView viewBtn;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            if (convertView == null) {

                holder=new ViewHolder();

                convertView = mInflater.inflate(R.layout.active_item, null);
                holder.title = (TextView)convertView.findViewById(R.id.title);
                holder.info = (TextView)convertView.findViewById(R.id.info);
                holder.viewBtn = (TextView)convertView.findViewById(R.id.view_btn);
                convertView.setTag(holder);

            }else {
                holder = (ViewHolder)convertView.getTag();
            }
            final String title = (String)mData.get(position).get("title");
            final String dnum = (String)mData.get(position).get("info");
            holder.title.setText(title);
            holder.info.setText(dnum);
            holder.viewBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(MainListActivity.this, ItemListActivity.class);
                    intent.putExtra("title", title);
                    intent.putExtra("dnum", dnum);
                    startActivity(intent);
                    //finish();//停止当前的Activity,如果不写,则按返回键会跳转回原来的Activity
                }
            });
            return convertView;
        }
    }
}

