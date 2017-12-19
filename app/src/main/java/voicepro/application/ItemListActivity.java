package voicepro.application;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import voicelibs.utils.SQliteManager;

/**
 * Created by yingy on 2017/12/14.
 */


public class ItemListActivity extends Activity {

    @BindView(R.id.list2) ListView list2;
    @BindView(R.id.headert) TextView headt;
    @BindView(R.id.num) TextView tnum;
    @BindView(R.id.back_btn) TextView back_btn;

    private SQliteManager sqm;
    private String title;
    private String dnum;
    private List<Map<String, Object>> mData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_listactive);
        ButterKnife.bind(this);
        title = this.getIntent().getStringExtra("title");
        dnum = this.getIntent().getStringExtra("dnum");
        sqm = new SQliteManager(ItemListActivity.this,"voice_db",null,1);
        mData = getData(title);
        initHeadView();
        MyAdapter adapter = new MyAdapter(this);
        list2.setAdapter(adapter);
        list2.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Intent intent = new Intent();
                intent.setClass(ItemListActivity.this, EchoActivity.class);
                String wid = (String)mData.get(position).get("id");
                String title  = (String)mData.get(position).get("title");
                String filename = (String)mData.get(position).get("filename");
                intent.putExtra("wid",wid);
                intent.putExtra("title",title);
                intent.putExtra("filename",filename);
                startActivity(intent);
            }
        });
    }
    public void initHeadView(){
        tnum.setText(dnum);
        headt.setText(title);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();//停止当前的Activity,如果不写,则按返回键会跳转回原来的Activity
            }
        });
    }

    private List<Map<String, Object>> getData(String wcase) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteDatabase db = sqm.getReadableDatabase();
        Cursor cursor = db.query("wav_table", new String[]{"id","filename","context","read_num"}, "wcase=?",new String[]{wcase} , null, null, null);
        while(cursor.moveToNext()){
            String title = cursor.getString(cursor.getColumnIndex("context"));
            int read_num = cursor.getInt(cursor.getColumnIndex("read_num"));
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String filename = cursor.getString(cursor.getColumnIndex("filename"));
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("title", title);
            map.put("info", String.valueOf(read_num));
            map.put("id", String.valueOf(id));
            map.put("filename",filename);
            list.add(map);
        }
        //关闭数据库
        db.close();
        return list;
    }
/*
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(ItemListActivity.this, EchoActivity.class);
        String wid = (String)mData.get(position).get("id");
        String title  = (String)mData.get(position).get("title");
        String filename = (String)mData.get(position).get("filename");
        intent.putExtra("wid",wid);
        intent.putExtra("title",title);
        intent.putExtra("filename",filename);
        startActivity(intent);
    }
*/
    public final class ViewHolder{
        public TextView title;
        public TextView info;
        public Button viewBtn;
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            if (convertView == null) {

                holder=new ViewHolder();

                convertView = mInflater.inflate(R.layout.list_item, null);
                holder.title = (TextView)convertView.findViewById(R.id.title);
                holder.info = (TextView)convertView.findViewById(R.id.info);
                convertView.setTag(holder);

            }else {

                holder = (ViewHolder)convertView.getTag();
            }
            holder.title.setText((String)mData.get(position).get("title"));
            holder.info.setText((String)mData.get(position).get("info"));
            return convertView;
        }
    }
}