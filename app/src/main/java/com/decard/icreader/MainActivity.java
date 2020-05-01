package com.decard.icreader;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.decard.NDKMethod.BasicOper;
import com.decard.entitys.IDCard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;


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

    /** Called when the user taps the button_open */
    public void open_reader(View view) {
        String str;
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

    /** Called when the user taps the button_close */
    public void close_reader(View view) {
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

    /** Called when the user taps the button_m1card */
    public void operate_m1card(View view) {
        String str;
        String result = null;
        String[] resultArr = null;
        result = BasicOper.dc_reset();
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_reset", "success");
        } else {
            Log.d("dc_reset", "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            str = "dc_reset error code = " + resultArr[0] + " error msg = " + resultArr[1];
            myAddTextview(str);
        }
        result = BasicOper.dc_config_card(0x00);
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_config_card", "success ");
        } else {
            Log.d("dc_config_card", "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            str = "dc_config_card error code = " + resultArr[0] + " error msg = " + resultArr[1];
            myAddTextview(str);
            return;
        }
        result = BasicOper.dc_card_hex(0x01);
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_card_hex", "success card sn = " + resultArr[1]);
            str = "card sn:" + resultArr[1];
            myAddTextview(str);
        } else {
            Log.d("dc_card_hex", "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            str = "dc_card_hex error code = " + resultArr[0] + " error msg = " + resultArr[1];
            myAddTextview(str);
            return;
        }
        /*验证密钥()*/

        result = null;
        resultArr = null;
        result = BasicOper.dc_authentication_pass(0, 1, "FFFFFFFFFFFF");
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_authentication", "success");
            //myAddTextview("dc_authentication success");
        } else {
            Log.d("dc_authentication", "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            str = "dc_authentication " + "error code = " + resultArr[0] + " error msg = " + resultArr[1];
            myAddTextview(str);
            return;
        }


        result = null;
        resultArr = null;
        String str1 = rndString();
        result = BasicOper.dc_write_hex(4, str1);
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_write_hex", "success");
            myAddTextview("write data:" + str1);
        } else {
            Log.d("dc_write_hex", "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
        }

        result = null;
        resultArr = null;
        result = BasicOper.dc_read_hex(4);
        resultArr = result.split("\\|", -1);
        if (resultArr[0].equals("0000")) {
            Log.d("dc_read_hex", "success data : " + resultArr[1]);
            str = "read data :" + resultArr[1];
            myAddTextview(str);
        } else {
            Log.d("dc_read_hex", "error code = " + resultArr[0] + " error msg = " + resultArr[1]);
            str = "dc_read_hex " + "error code = " + resultArr[0] + " error msg = " + resultArr[1];
            myAddTextview(str);
            return;
        }
        str = "M1卡测试成功";
        myAddTextview(str);
    }
    /** Called when the user taps the button_cpucard */
    public void operate_cpucard(View view) {
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
    /** Called when the user taps the button_idcard */
    public void read_idcard(View view) {
        IDCard idCard = BasicOper.dc_SamAReadCardInfo(1);
        if(idCard!=null) {
            myAddTextview("姓名："+idCard.getName());
            myAddTextview("性别："+idCard.getSex());
            myAddTextview("民族："+idCard.getNation());
            myAddTextview("出生日期："+idCard.getBirthday());
            myAddTextview("地址："+idCard.getAddress());
            myAddTextview("身份证号："+idCard.getId());
            myAddTextview("发证机关："+idCard.getOffice());
            myAddTextview("有效期："+idCard.getEndTime());
            ImageView imageView = (ImageView) findViewById(R.id.imageView_iccard);
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(idCard.getPhotoData(), 0, idCard.getPhotoData().length));
            imageView.setVisibility(View.VISIBLE);
        }
        else{
            myAddTextview("读身份证失败！");
        }
        return ;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView imageView = (ImageView) findViewById(R.id.imageView_iccard);
        imageView.setVisibility(View.INVISIBLE);
        Button clearData = findViewById(R.id.button_clear);
        clearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView imageView = (ImageView) findViewById(R.id.imageView_iccard);
                imageView.setVisibility(View.INVISIBLE);
                TextView tv = findViewById(R.id.messageText);
                tv.setText("");
                return;
            }});
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
