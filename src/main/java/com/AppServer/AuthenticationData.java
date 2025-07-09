package com.AppServer;

import java.io.Serial;
import java.io.Serializable;

public class AuthenticationData implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;
    Boolean newAccount;
    String username;
    String password;
    String name;

    //optional
    //Image profilePicture;
    //email

    // this Constructor will be called when Authentication data hold login information
    AuthenticationData(String username, String password){
        this.username = username;
        this.password = password;
        newAccount = false;
    }
    // this Constructor will be called when Authentication data hold login information
    AuthenticationData(String username,String name, String password){
        this.username = username;
        this.password = password;
        this.name = name;
        newAccount = true;
    }
}
