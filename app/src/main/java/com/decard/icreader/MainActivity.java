package com.decard.icreader;

import android.os.Bundle;

import com.decard.NDKMethod.BasicOper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
//import com.decard.NDKMethod;

public class MainActivity extends AppCompatActivity {
    private void myAddTextview(String str){
        TextView tv = findViewById(R.id.messageText);
        tv.append(str+"\n");
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
    }
    private String rndString()
    {
        String str="";
        String[] strtemp = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        for(int i=0;i<32;i++)
        {
            Random ra =new Random();
            int tempi = ra.nextInt(15);
            str=str+strtemp[tempi];
        }
        return str;
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        Button clearData = findViewById(R.id.clearData);
        clearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView tv = findViewById(R.id.messageText);
                tv.setText("");
                return;
            }});
        Button openDevic = findViewById(R.id.openDevice);
        openDevic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str;
                //BasicOper.dc_AUSB_ReqPermission(getApplicationContext());
                /*try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                try {
                    FileWriter localFileWriter = new FileWriter(new File("/sys/class/ugp_ctrl/gp_sys_5v_ctrl/enable"));
                    localFileWriter.write("1");
                    localFileWriter.close();
                }catch (IOException localIOException)
                {
                    localIOException.printStackTrace();
                    return;
                }
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int devHandle = BasicOper.dc_open("COM",null,"/dev/ttyHSL1",115200);//返回值为设备句柄号。
                if(devHandle>0){
                    Log.d("open","dc_open success devHandle = "+devHandle);
                    str = "打开端口成功" ;
                    myAddTextview(str);
                }else{
                str = "打开端口失败" ;
                myAddTextview(str);
                }
                return;
            }
        });
        Button closeDevic = findViewById(R.id.closeDevice);
        closeDevic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BasicOper.dc_exit();
                try {
                    FileWriter localFileWriter = new FileWriter(new File("/sys/class/ugp_ctrl/gp_sys_5v_ctrl/enable"));
                    localFileWriter.write("0");
                    localFileWriter.close();
                } catch (IOException localIOException) {
                    localIOException.printStackTrace();
                }
                myAddTextview("关闭端口");
            }
        });
        Button nonAccessM1 = findViewById(R.id.nonAccessM1);
        nonAccessM1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str;
                String result = null;
                String[] resultArr = null;
                result = BasicOper.dc_reset();
                resultArr = result.split("\\|",-1);
                if(resultArr[0].equals("0000")){
                    Log.d("dc_reset","success");
                }else{
                    Log.d("dc_reset","error code = "+resultArr[0] +" error msg = "+resultArr[1] );
                    str = "dc_reset error code = "+ resultArr[0] + " error msg = " + resultArr[1];
                    myAddTextview(str);
                }
                result = BasicOper.dc_config_card(0x00);
                resultArr = result.split("\\|",-1);
                if(resultArr[0].equals("0000")){
                    Log.d("dc_config_card","success ");
                }else{
                    Log.d("dc_config_card","error code = "+resultArr[0] +" error msg = "+resultArr[1] );
                    str = "dc_config_card error code = "+ resultArr[0] + " error msg = " + resultArr[1];
                    myAddTextview(str);
                    return;
                }
                result = BasicOper.dc_card_hex(0x01);
                resultArr = result.split("\\|",-1);
                if(resultArr[0].equals("0000")){
                    Log.d("dc_card_hex","success card sn = " + resultArr[1]);
                    str = "card sn:"+resultArr[1];
                    myAddTextview(str);
                }else{
                    Log.d("dc_card_hex","error code = "+resultArr[0] +" error msg = "+resultArr[1] );
                    str = "dc_card_hex error code = "+ resultArr[0] + " error msg = " + resultArr[1];
                    myAddTextview(str);
                    return;
                }
                /*验证密钥()*/

                result = null;
                resultArr = null;
                result = BasicOper.dc_authentication_pass(0,1, "FFFFFFFFFFFF");
                resultArr = result.split("\\|",-1);
                if(resultArr[0].equals("0000")){
                    Log.d("dc_authentication","success");
                    //myAddTextview("dc_authentication success");
                }else{
                    Log.d("dc_authentication","error code = "+resultArr[0] +" error msg = "+resultArr[1] );
                    str = "dc_authentication "+"error code = "+resultArr[0] +" error msg = "+resultArr[1] ;
                    myAddTextview(str);
                    return;
                }


                result = null;
                resultArr = null;
                String str1 = rndString();
                result = BasicOper.dc_write_hex(4,str1);
                resultArr = result.split("\\|",-1);
                if(resultArr[0].equals("0000")){
                    Log.d("dc_write_hex","success");
                    myAddTextview("write data:"+str1);
                }else{
                    Log.d("dc_write_hex","error code = "+resultArr[0] +" error msg = "+resultArr[1] );
                }

                result = null;
                resultArr = null;
                result = BasicOper.dc_read_hex(4);
                resultArr = result.split("\\|",-1);
                if(resultArr[0].equals("0000")){
                    Log.d("dc_read_hex","success data : "+resultArr[1]);
                    str = "read data :"+resultArr[1];
                    myAddTextview(str);
                }else{
                    Log.d("dc_read_hex","error code = "+resultArr[0] +" error msg = "+resultArr[1] );
                    str = "dc_read_hex "+"error code = "+resultArr[0] +" error msg = "+resultArr[1] ;
                    myAddTextview(str);
                    return;
                }
                str = "M1卡测试成功";
                myAddTextview(str);
            }
            });
        Button nonAccessCpu = findViewById(R.id.nonAccessCpu);
        nonAccessCpu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str;

                String result = BasicOper.dc_reset();
                String[] resultArr = result.split("\\|",-1);
                if(resultArr[0].equals("0000")){
                    Log.d("dc_reset","success");
                }else{
                    Log.d("dc_reset","error code = "+resultArr[0] +" error msg = "+resultArr[1] );
                    str = "dc_reset error code = "+ resultArr[0] + " error msg = " + resultArr[1];
                    myAddTextview(str);
                }
                result = BasicOper.dc_config_card(0x00);
                resultArr = result.split("\\|",-1);
                if(resultArr[0].equals("0000")){
                    Log.d("dc_config_card","success ");
                }else{
                    Log.d("dc_config_card","error code = "+resultArr[0] +" error msg = "+resultArr[1] );
                    str = "dc_config_card error code = "+ resultArr[0] + " error msg = " + resultArr[1];
                    myAddTextview(str);
                    return;
                }
                result = BasicOper.dc_card_hex(0x01);
                resultArr = result.split("\\|",-1);
                if(resultArr[0].equals("0000")){
                    Log.d("dc_card_hex","success card sn = " + resultArr[1]);
                    str = "card sn:"+resultArr[1];
                    myAddTextview(str);
                }else{
                    Log.d("dc_card_hex","error code = "+resultArr[0] +" error msg = "+resultArr[1] );
                    str = "dc_card_hex error code = "+ resultArr[0] + " error msg = " + resultArr[1];
                    myAddTextview(str);
                    return;
                }


                result = BasicOper.dc_pro_resethex();
                resultArr = result.split("\\|",-1);
                if(resultArr[0].equals("0000")){
                    Log.d("dc_pro_resethex","success ATR/ATS = " + resultArr[1]);
                    str = "ATR/ATS:" +resultArr[1];
                    myAddTextview(str);
                }else{
                    Log.d("dc_pro_resethex","error code = "+resultArr[0] +" error msg = "+resultArr[1] );
                    str = "dc_pro_resethex error code = " + resultArr[0] + " error msg = " + resultArr[1];
                    myAddTextview(str);
                    return;
                }

                result = BasicOper.dc_pro_commandhex("0084000008",7);
                str = "send apdu:"+"0084000008";
                myAddTextview(str);
                resultArr = result.split("\\|",-1);
                if(resultArr[0].equals("0000")){
                    Log.d("dc_pro_commandhex","success reponse apdu = " + resultArr[1]);
                    str = "reponse apdu:"+resultArr[1];
                    myAddTextview(str);
                }else{
                    Log.d("dc_pro_commandhex","error code = "+resultArr[0] +" error msg = "+resultArr[1] );
                    str = "dc_pro_commandhex error code = "+ resultArr[0] + " error msg = " + resultArr[1];
                    myAddTextview(str);
                    return;
                }
                return;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
