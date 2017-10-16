/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package com.timeandtidestudio.emergencybroadcast.Controller.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.timeandtidestudio.emergencybroadcast.Controller.common.Constants;
import com.timeandtidestudio.emergencybroadcast.R;
import com.timeandtidestudio.emergencybroadcast.wizard.FloatingHintEditText;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by samyboy89 on 08/11/14.
 */
public class NextOfKinDialog extends Dialog {

    @BindView(R.id.dialog_next_of_kin_name)           public FloatingHintEditText mNameEdit;
    @BindView(R.id.dialog_next_of_kin_telephone)      public FloatingHintEditText mTelephoneEdit;
    @BindView(R.id.ok_button)                         public View mOK;
    @BindView(R.id.cancel_button)                     public View mCancel;

    @SuppressLint("NewApi")
    public NextOfKinDialog(final Context context) {
        super(context);

        final WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount = 0.0f;
        lp.alpha = 1f;
        lp.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;

        setTitle(R.string.wizard_next_of_kin_header);
        setContentView(R.layout.dialog_next_of_kin);
        ButterKnife.bind(this);

        String name = PreferencesHelper.getString(Constants.PREFS_NEXT_OF_KIN_NAME);
        if (!name.equals(PreferencesHelper.INVALID_STRING)) mNameEdit.setText(name);

        String telephone = PreferencesHelper.getString(Constants.PREFS_NEXT_OF_KIN_TELEPHONE);
        if (!telephone.equals(PreferencesHelper.INVALID_STRING)) mTelephoneEdit.setText(telephone);

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        mOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNameEdit.getText().length() == 0) {
                    Toast.makeText(getContext(), R.string.wizard_next_of_kin_name_not_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mTelephoneEdit.getText().length() == 0) {
                    Toast.makeText(getContext(), R.string.wizard_next_of_kin_telephone_not_empty, Toast.LENGTH_SHORT).show();
                    return;
                }

                PreferencesHelper.putString(Constants.PREFS_NEXT_OF_KIN_NAME, mNameEdit.getText().toString());
                PreferencesHelper.putString(Constants.PREFS_NEXT_OF_KIN_TELEPHONE, mTelephoneEdit.getText().toString());
                dismiss();
            }
        });

        show();

        getWindow().getDecorView().setSystemUiVisibility(((Activity) context).getWindow().getDecorView().getSystemUiVisibility());
    }

}
