package com.decard.icreader;

import android.device.IccManager;
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
import android.widget.ProgressBar;
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
        int offset=tv.getLineCount()*tv.getLineHeight();
        if(offset>tv.getHeight()){
            tv.scrollTo(0,offset-tv.getHeight());
        }
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
    private  String bytesToHexString(byte[] src, int offset, int length) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = offset; i < length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
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

        result = BasicOper.dc_pro_commandhex("00a404000E315041592E5359532E4444463031",7);
        str = "send apdu:"+"00a404000E315041592E5359532E4444463031";
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

    public void operate_samcard(View view){
        IccManager mIccReader = new IccManager();
        int st , retlen;
        String str;
        byte[] atr = new byte[128];
        byte[] rspBuf = new byte[300];
        byte[] rspStatus = new byte[2];
        st = mIccReader.open((byte)1, (byte)0x01, (byte)0x2);  //参数1 = 1，选择SAM1
        if(st!=0) {
            str = "选择操作SAM1卡错误 error code = "+ st ;
            myAddTextview(str);
            return;
        }
        myAddTextview("选择操作SAM1卡");
        retlen = mIccReader.activate(atr);
        if(retlen <= 0) {
            str = "SAM1卡上电复位失败";
            myAddTextview(str);
            mIccReader.close();
            return;
        }
        str ="SAM1卡复位成功："+ bytesToHexString(atr,0,retlen) ;
        myAddTextview(str);

        //SAM卡取随机数命令示例 0084000008
        byte[] apdu_buf = {
                0x00, (byte)0x84, 0x00, 0x00, 0x08
        };

        str ="APDU命令数据："+ bytesToHexString(apdu_buf,0,5) ;
        myAddTextview(str);
        retlen = mIccReader.apduTransmit(apdu_buf,5,rspBuf,rspStatus);
        if(retlen == -1) {
            str = "SAM1卡发送APDU命令失败";
            myAddTextview(str);
            mIccReader.close();
            return;
        }
        str ="APDU返回数据："+ bytesToHexString(rspBuf,0,retlen) ;
        myAddTextview(str);
        str ="APDU返回状态："+ bytesToHexString(rspStatus,0,2) ;
        myAddTextview(str);

        mIccReader.close();
        return;
    }

    public void operate_contactcpucard(View view){
        IccManager mIccReader = new IccManager();
        int st , retlen;
        String str;
        byte[] atr = new byte[128];
        byte[] rspBuf = new byte[300];
        byte[] rspStatus = new byte[2];
        st = mIccReader.open((byte)0, (byte)0x01, (byte)0x2);
        if(st!=0) {
            str = "选择操作接触式CPU卡错误 error code = "+ st ;
            myAddTextview(str);
            return;
        }
        myAddTextview("选择操作接触式CPU大卡");
        st= mIccReader.detect();
        if(st!=0){
            str = "没有检测到接触式IC卡 error code = "+ st ;
            myAddTextview(str);
            mIccReader.close();
            return;
        }
        myAddTextview("检测到接触式大卡插入");
        retlen = mIccReader.activate(atr);
        if(retlen <=0) {
            str = "接触式CPU卡上电复位失败";
            myAddTextview(str);
            mIccReader.close();
            return;
        }
        str ="CPU卡复位成功："+ bytesToHexString(atr,0,retlen) ;
        myAddTextview(str);

        //金融CPU卡(SELECT)命令示例,请求选择 PSE(文件名“1PAY.SYS.DDF01”) 00a404000E315041592E5359532E4444463031
        byte[] apdu_buf = {
                0x00, (byte) 0xA4, 0x04, 0x00, 0x0E, 0x31, 0x50, 0x41, 0x59, 0x2E, 0x53,
                0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31
        };
        str ="APDU命令数据："+ bytesToHexString(apdu_buf,0,19) ;
        myAddTextview(str);
        retlen = mIccReader.apduTransmit(apdu_buf,19,rspBuf,rspStatus);
        if(retlen == -1) {
            str = "接触式CPU卡发送APDU命令失败";
            myAddTextview(str);
            mIccReader.close();
            return;
        }
        str ="APDU返回数据："+ bytesToHexString(rspBuf,0,retlen) ;
        myAddTextview(str);
        str ="APDU返回状态："+ bytesToHexString(rspStatus,0,2) ;
        myAddTextview(str);

        st = mIccReader.deactivate();
        if(st!=0){
            str = "接触式IC卡下电失败 error code = "+ st ;
            myAddTextview(str);
            mIccReader.close();
            return;
        }
        myAddTextview("接触式IC卡下电成功！");
        mIccReader.close();
        return;
    }


    MsrCardTask transTask ;
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
                tv.scrollTo(0,0);
                return;
            }
        });

        Button msrCardStart = findViewById(R.id.button_msrcardstart);
        msrCardStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = findViewById(R.id.messageText);
                ProgressBar progressbar = (ProgressBar) findViewById(R.id.progressBar_msrcard);
                MsrCardTask asyncTask = new MsrCardTask(tv, progressbar);
                transTask = asyncTask;
                asyncTask.execute();
                return;
            }
        });
        Button msrCardStop = findViewById(R.id.button_msrcardstop);
        msrCardStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transTask.cancel(true);
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
