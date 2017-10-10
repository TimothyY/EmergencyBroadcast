package com.timeandtidestudio.emergencybroadcast.Model;

import java.io.Serializable;

/**
 * Created by User on 9/25/2017.
 */

public class SentMessage implements Serializable{

    public String date;
    public String hour;
    public String message;
    public String timestamp;

    public SentMessage(String timestamp,String message) {
        this.timestamp = timestamp;
        this.date = timestamp;
        this.hour = timestamp;
        this.message = message;
    }

}
