package org.innovateuk.ifs.user.resource;

public enum ProfileRole {
    ASSESSOR("assessor");

    private String name;

    ProfileRole(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
