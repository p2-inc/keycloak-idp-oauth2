package io.phasetwo.keycloak.oauth2idp.model;

import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Map;

@Data
public class BrokeredUserProfile {

    private String username;
    private String firstName;
    private String lastName;
    private String email;

    private Map<String, Object> contextData = Maps.newHashMap();
}
