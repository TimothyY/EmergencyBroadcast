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

package com.timeandtidestudio.emergencybroadcast.Controller.common;

public class ClientPaths {
    public static final String MODE_PULL = "/pull";
    public static final String MODE_PUSH = "/push";
    public static final String MODE_DEFAULT = MODE_PUSH;
    public static final String START_PUSH = "/start_push";
    public static final String START_ALARM = "/start_alarm";
    public static final String STOP_ALARM = "/stop_alarm";
    public static final String ALARM_PROGRESS = "/progress/";
}
