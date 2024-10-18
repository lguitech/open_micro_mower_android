/*********************************************************************
 *
 *  This file is part of the [OPEN_MICRO_MOWER_ANDROID] project.
 *  Licensed under the MIT License for non-commercial purposes.
 *  Author: Brook Li
 *  Email: lguitech@126.com
 *
 *  For more details, refer to the LICENSE file or contact [lguitech@126.com].
 *
 *  Commercial use requires a separate license.
 *
 *  This software is provided "as is", without warranty of any kind.
 *
 *********************************************************************/

package com.micronavi.mower.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.micronavi.mower.R;
import java.lang.ref.WeakReference;

public class WelcomeActivity extends AppCompatActivity {
    private static MyHandler handler;

    private static class MyHandler extends Handler {
        private final WeakReference<WelcomeActivity> mActivity;
        public MyHandler(WelcomeActivity activity) {
            mActivity = new WeakReference<WelcomeActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            if (mActivity.get() != null) {
                switch(msg.what) {
                    case 100:
                        mActivity.get().startMainActivity();
                        break;
                    default:
                        break;
                }

            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        handler = new MyHandler(this);
        handler.sendEmptyMessageDelayed(100, 1000);
    }
    protected void startMainActivity()
    {
        Intent intent = new Intent(WelcomeActivity.this,
                MainActivity.class);
        startActivity(intent);
        finish();
    }

}