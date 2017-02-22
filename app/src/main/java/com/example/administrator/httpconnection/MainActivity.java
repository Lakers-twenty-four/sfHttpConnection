package com.example.administrator.httpconnection;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText et_number;
    private TextView tv_body;
    private Gson mGson;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case LOAD_ERROR:
                    tv_body.setText((String)msg.obj);
                    break;
                case LOAD_SUCCESSFUL:
                    StringBuffer stringBuffer = new StringBuffer();
                    for(ExpressCompanyDate.ResultBean.ListBean bean : mList){
                        stringBuffer.append(bean.getDatetime()+bean.getRemark()+"\n");
                    }
            }
        }
    };
    private final int LOAD_ERROR = 100;
    private List<ExpressCompanyDate.ResultBean.ListBean> mList;
    private final int LOAD_SUCCESSFUL = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化视图
        initView();
    }

    private void initView() {
        et_number= (EditText) findViewById(R.id.et_number);
        tv_body= (TextView) findViewById(R.id.tv_body);
    }

    public void refer(View v){
        new Thread(){
            public void run() {
                try {
                    // [1]获取源码路径
                    String number=et_number.getText().toString().trim();
                    //[2]创建URL 对象指定我们要访问的网址（路径）
                    URL url=new URL("http://v.juhe.cn/exp/index?key=0d52fd004fb547efb5ee4e2f10d43a43&com=sf&no="+number);
                    // [3]拿到httpurlconnection对象 用于发送或者接受数据
                    HttpURLConnection kb= (HttpURLConnection) url.openConnection();
                    // [4]设置发送get请求
                    kb.setRequestMethod("GET");
                    //[5]设置请求的超时时间
                    kb.setReadTimeout(5000);
                    //[6]获取服务器返回的状态码
                    int code=kb.getResponseCode();
                    // [7]如果code==200说明请求成功
                    //200（成功）  服务器已成功处理了请求。通常，这表示服务器提供了请求的网页。
                    if(code==200){
                        // [8]获取服务器返回的数据，是以流的形式返回的
                        InputStream in=kb.getInputStream();
                        String content=readStream.read(in);
                        System.out.println(content);
                        System.out.println(content.length());
                        if(content.length() < 74){
                            JSONObject jsonObject = new JSONObject(content);
                            String reason = jsonObject.getString("reason");
                            System.out.println(reason);
                            Message msg = Message.obtain();
                            msg.obj = reason;
                            msg.what = LOAD_ERROR;
                            handler.sendMessage(msg);
                        }else {
                            ExpressCompanyDate u = new ExpressCompanyDate();
                            u = mGson.fromJson(content, ExpressCompanyDate.class);
                            mList = u.getResult().getList();
                            handler.sendEmptyMessage(LOAD_SUCCESSFUL);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();




    }
}
