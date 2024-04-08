package com.tech.spotify.config.oauth.provider;

import java.util.Map;

public class NaverUserInfo implements OAuth2UserInfo {

    private Map<String, Object> attributes; // OAuth2.getAttributes()

    // id=oG1701Ob0akJwXK2fLCaP0hDNvx-c2VBFE5k4OdXdtY, email=fate427@naver.com, name=신재환
    public NaverUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("id"); 
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
}
