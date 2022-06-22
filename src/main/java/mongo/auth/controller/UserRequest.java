package mongo.auth.controller;

import lombok.Data;
import mongo.auth.security.Utenti;

@Data
public class UserRequest {

    private Utenti utente;
    private String adminUser;
    private String adminPassword;

}
