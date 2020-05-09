package com.decard.icreader;

import android.device.MagManager;
import android.os.AsyncTask;
import android.text.method.ScrollingMovementMethod;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 步骤1：创建AsyncTask子类
 * 注：
 *   a. 继承AsyncTask类
 *   b. 为3个泛型参数指定类型；若不使用，可用java.lang.Void类型代替
 *      此处指定为：输入参数 = String类型、执行进度 = Integer类型、执行结果 = String类型
 *   c. 根据需求，在AsyncTask子类内实现核心方法
 */
public class MsrCardTask extends AsyncTask<String, Integer, String> {


    private final TextView textView;
    private final ProgressBar progressBar;
    private MagManager magManager;
    String str;

    public MsrCardTask(TextView textView, ProgressBar progressBar) {
        super();
        this.textView = textView;
        this.progressBar = progressBar;
    }

    private void myAddTextview(TextView tv, String str){
        tv.append(str+"\n");
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        int offset=tv.getLineCount()*tv.getLineHeight();
        if(offset>tv.getHeight()){
            tv.scrollTo(0,offset-tv.getHeight());
        }
    }

        // 方法1：onPreExecute（）
        // 作用：执行 线程任务前的操作
        @Override
        protected void onPreExecute() {
            myAddTextview(textView,"请刷磁条卡");
            magManager = new MagManager();
            if (magManager != null) {
                int ret = magManager.open();
                if (ret != 0) {
                    str = "打开磁条模块错误 error code = "+ ret ;
                    myAddTextview(textView,str);
                    return;
                }
            }
            else{
               myAddTextview(textView,"new MagManager() error!");
            }
            return;
            // 执行前显示提示
        }

        // 方法2：doInBackground（）
        // 作用：接收输入参数、执行任务中的耗时操作、返回 线程任务执行的结果
        // 此处通过计算从而模拟“加载进度”的情况
        @Override
        protected String doInBackground(String... params) {
            try {
                int count = 0;
                int length = 1;
                while (count<99) {

                    count += length;
                    // 可调用publishProgress（）显示进度, 之后将执行onProgressUpdate（）
                    publishProgress(count);
                    if (magManager != null ){
                        int ret = magManager.checkCard();
                        if(ret==0){
                            return "OK";
                        }
                    }
                    // 模拟耗时任务
                    Thread.sleep(50);
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "NO";
        }

        // 方法3：onProgressUpdate（）
        // 作用：在主线程 显示线程任务执行的进度
        @Override
        protected void onProgressUpdate(Integer... progresses) {

            progressBar.setProgress(progresses[0]);

        }

        // 方法4：onPostExecute（）
        // 作用：接收线程任务执行结果、将执行结果显示到UI组件
        @Override
        protected void onPostExecute(String result) {
            // 执行完毕后，则更新UI
            if(result == "OK"){
                myAddTextview(textView,"检测到刷卡");
                StringBuffer trackOne = new StringBuffer();
                byte[] stripInfo = new byte[1024];
                int allLen = magManager.getAllStripInfo(stripInfo);
                if (allLen > 0) {
                    int len = stripInfo[1];
                    if (len != 0)
                        myAddTextview(textView, " track1: " + new String(stripInfo, 2, len));
                    int len2 = stripInfo[3 + len];
                    if (len2 != 0)
                        myAddTextview(textView, " \ntrack2: " + new String(stripInfo, 4 + len, len2));
                    int len3 = stripInfo[5 + len + len2];
                    if (len3 != 0 && len3 < 1024)
                        myAddTextview(textView, " \ntrack3: " + new String(stripInfo, 6 + len + len2, len3));
                }
                else{
                    myAddTextview(textView,"磁条卡数据为空");
                }
            }
            else{
                myAddTextview(textView,"等待刷卡超时");
            }
        }

        // 方法5：onCancelled()
        // 作用：将异步任务设置为：取消状态
        @Override
        protected void onCancelled() {
            myAddTextview(textView,"刷磁条卡取消");
        }
    }
