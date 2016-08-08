package com.example.piotr.guardianangel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Piotr on 22/01/2016.
 */
public class Content implements Serializable {
    private List<String> registration_ids;
    private Map<String, String> data;

    public void addRegId(String regId){
        if(registration_ids == null){
            registration_ids = new LinkedList<String>();
        }
        registration_ids.add(regId);
    }

    public void createData(String lat, String lon, String who, String phone){
        if(data == null)
            data = new HashMap<String, String>();
        data.put("lat", lat);
        data.put("lon", lon);
        data.put("who", who);
        data.put("phone", phone);
        System.out.println("Name:"+data.get(who)+" phone: "+data.get(phone)+" lat " + data.get("lat") + " lon " + data.get("lon"));
    }
}
