package com.edeqa.waytousserver.helpers;

import com.edeqa.waytousserver.interfaces.DataProcessorConnection;

import java.util.HashMap;
import java.util.Map;

public class UserRequests {

    Map<String, UserRequest> map;

    public UserRequests() {
        map = new HashMap<>();
    }

    public void add(UserRequest userRequest) {
        map.put(userRequest.getAddress(), userRequest);
    }

    public void remove(UserRequest userRequest) {
        if(map.containsKey(userRequest.getAddress())) map.remove(userRequest.getAddress());
    }

    public UserRequest findByConnection(DataProcessorConnection dataProcessorConnection) {
        if(map.containsKey(dataProcessorConnection.getRemoteSocketAddress().toString())) {
            return map.get(dataProcessorConnection.getRemoteSocketAddress().toString());
        }
        return null;
    }

    public void remove(DataProcessorConnection dataProcessorConnection) {
        if(map.containsKey(dataProcessorConnection.getRemoteSocketAddress().toString())) map.remove(dataProcessorConnection.getRemoteSocketAddress().toString());

    }
}
