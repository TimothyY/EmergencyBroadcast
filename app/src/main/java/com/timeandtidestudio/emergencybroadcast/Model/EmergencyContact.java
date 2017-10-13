package com.timeandtidestudio.emergencybroadcast.Model;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Serializable;

/**
 * Created by User on 9/25/2017.
 */

public class EmergencyContact implements Serializable, Comparable<EmergencyContact>{

    public int id;
    public String name;
    public String phone;
    public String initial;

    public EmergencyContact(String name, String phone) {
        this.name = name;
        this.phone = phone;
        if(name.contains(" ")){
            this.initial = ""+name.substring(0,1) + name.substring(name.indexOf(" ")+1,name.indexOf(" ")+2);
            this.initial = this.initial.toUpperCase();
        }else{
            this.initial = name.substring(0,1).toUpperCase() + name.substring(1,2).toLowerCase();
        }
    }

    public EmergencyContact(int id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        if(name.contains(" ")){
            this.initial = ""+name.substring(0,1) + name.substring(name.indexOf(" ")+1,name.indexOf(" ")+2);
            this.initial = this.initial.toUpperCase();
        }else{
            this.initial = name.substring(0,1).toUpperCase() + name.substring(1,2).toLowerCase();
        }
    }

    @Override
    public int compareTo(@NonNull EmergencyContact o) {
        return this.name.compareTo(o.name);
    }
}
