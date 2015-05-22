/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.simplesample;

import org.gearvrf.GVRActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SampleActivity extends GVRActivity {

    private TextView mTextView;
    private LinearLayout mTextViewWrapper;
	
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setTextView();
        setScript(new SampleViewManager(this), "gvr_note4.xml");
    }
    
	private void setTextView(){
		mTextView = new TextView(this);
		mTextView.setLayoutParams(new LayoutParams(2000,1000));		
		mTextView.measure(2000, 1000);
//		mTextView.layout(0, 0, 2000, 1000);
		mTextView.setText("asodfjaoidjfioajfioajwofijaoiefjaoiwejfiojaodfjiuwajedofkjeaoiwjfoiawjfoiaerhgfuiawjfeoiaerjhgroajegoijoifjaeroighaiujfojaeoirghoawjefoijawgfjaeoirghjoiawjgfoiueahrgoiajweoigfharjojfgaoiejhgoiawjgoijaeroigjawoijgieaurjgfoaejgiuoajfoiajergoijaoijoigrjoagrjoei");
		mTextView.setBackgroundColor(Color.TRANSPARENT);
		mTextView.setTextColor(Color.RED);
		mTextView.setVisibility(View.VISIBLE);
		mTextViewWrapper = new LinearLayout(this);
		//mTextViewWrapper.setLayoutParams(new LayoutParams(2000,1000));		
		mTextViewWrapper.addView(mTextView);
		mTextViewWrapper.measure(2000, 1000);
		mTextViewWrapper.layout(0, 0, 2000, 1000);
		mTextViewWrapper.setBackgroundColor(Color.CYAN);
		mTextViewWrapper.setVisibility(View.VISIBLE);
	}
    
	
	LinearLayout getTextView(){
		return mTextViewWrapper;
	}
}
