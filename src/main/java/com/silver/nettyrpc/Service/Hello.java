package com.silver.nettyrpc.Service;

import com.silver.nettyrpc.server.ServiceRPC;

@ServiceRPC(value=Hello.class)
public class Hello {
    public void hello(){
        System.out.println("Hello World");
    }
}
