
package com.sprd.validationtools.testinfo;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;
import android.media.MediaDrm;
import java.util.UUID;

public class GoogleDRMVersionTest extends BaseActivity {

    public static final UUID WIDEVINE_UUID = new UUID(0xEDEF8BA979D64ACEL, 0xA3C827DCD51D21EDL);
    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;
    private TextView textView6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drm_version);
        setTitle("drm test");
        initView();
        initDate();
    }

    /**
     * 初始化数据*/
    private void initDate() {
        try {
            MediaDrm drm = new MediaDrm(WIDEVINE_UUID);
            String ALGORITHMS = drm.getPropertyString(MediaDrm.PROPERTY_ALGORITHMS);
            String DESCRIPTION = drm.getPropertyString(MediaDrm.PROPERTY_DESCRIPTION);
            String VENDOR = drm.getPropertyString(MediaDrm.PROPERTY_VENDOR);
            String VERSION = drm.getPropertyString(MediaDrm.PROPERTY_VERSION);
            String securityLevel = drm.getPropertyString("securityLevel");
            String systemId = drm.getPropertyString("systemId");
            textView1.setText("ALGORITHMS:"+ALGORITHMS);
            textView2.setText("DESCRIPTION:"+DESCRIPTION);
            textView3.setText("VENDOR:"+VENDOR);
            textView4.setText("VERSION:"+VERSION);
            textView5.setText("securityLevel:"+securityLevel);
            textView6.setText("systemId:"+systemId);
            //drm.getSecurityLevel()

            //Log.e("liujunjie", "getDrmInfo: al:"+al+",de:"+de+",ven:"+ven+",ver:"+ver+",drm:"+drm+",ll:"+ll);

        } catch (Exception e) {
            //throw new RuntimeException(e);
            Log.e("liujunjie", "getDrmInfo: "+e.getMessage() );
        }
    }


    /**
     * 初始化数据
     * */
    private void initView() {
        textView1 =  (TextView) findViewById(R.id.textview_1);
        textView2 =  (TextView) findViewById(R.id.textview_2);
        textView3 =  (TextView) findViewById(R.id.textview_3);
        textView4 =  (TextView) findViewById(R.id.textview_4);
        textView5 =  (TextView) findViewById(R.id.textview_5);
        textView6 =  (TextView) findViewById(R.id.textview_6);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}