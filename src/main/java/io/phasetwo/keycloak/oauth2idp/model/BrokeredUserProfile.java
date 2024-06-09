package io.phasetwo.keycloak.oauth2idp.model;

import lombok.Data;

@Data
public class BrokeredUserProfile {

    private String username;
    private String firstName;
    private String lastName;
    private String email;

    private Object mappingContext;
}
