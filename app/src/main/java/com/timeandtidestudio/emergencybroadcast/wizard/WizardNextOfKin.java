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

package com.timeandtidestudio.emergencybroadcast.wizard;


import com.timeandtidestudio.emergencybroadcast.Controller.common.Constants;
import com.timeandtidestudio.emergencybroadcast.Controller.utils.PreferencesHelper;
import com.timeandtidestudio.emergencybroadcast.R;

import butterknife.BindView;

/**
 * Created by samyboy89 on 26/02/15.
 */
public class WizardNextOfKin extends WizardTemplate {

    @BindView(R.id.wizard_next_of_kin_name)        public FloatingHintEditText mNameEdit;
    @BindView(R.id.wizard_next_of_kin_telephone)   public FloatingHintEditText mTelephoneEdit;

    public static WizardNextOfKin newInstance(int position) {
        return newInstance(WizardNextOfKin.class, R.layout.wizard_next_of_kin, position);
    }

    @Override
    public void init() {
        String name = PreferencesHelper.getString(Constants.PREFS_NEXT_OF_KIN_NAME);
        if (!name.equals(PreferencesHelper.INVALID_STRING)) mNameEdit.setText(name);

        String telephone = PreferencesHelper.getString(Constants.PREFS_NEXT_OF_KIN_TELEPHONE);
        if (!telephone.equals(PreferencesHelper.INVALID_STRING)) mTelephoneEdit.setText(telephone);

        mWizardMain.setFields(mNameEdit, mTelephoneEdit);
    }
}
