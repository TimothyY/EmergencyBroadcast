package com.timeandtidestudio.emergencybroadcast.Model;

/**
 * Created by User on 9/25/2017.
 */

public class SentMessage {

    public String title;
    public String content;
    public String timestamp;

    public SentMessage(String title, String content, String timestamp) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
    }
}
