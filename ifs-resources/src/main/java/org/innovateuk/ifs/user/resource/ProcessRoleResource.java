package org.innovateuk.ifs.user.resource;

public class ProcessRoleResource {
    private Long id;
    private Long user;
    private String userName;
    private Long applicationId;
    private Role role;
    private String roleName;
    private Long organisationId;
    private String userEmail;

    public ProcessRoleResource(){
    	// no-arg constructor
    }

    public ProcessRoleResource(Long id, UserResource user, Long applicationId, Role role, String roleName, Long organisationId) {
        this.id = id;
        this.user = user.getId();
        this.userName = user.getName();
        this.applicationId = applicationId;
        this.role = role;
        this.roleName = roleName;
        this.organisationId = organisationId;
    }

    public Long getId(){return id;}

    public Long getUser() {
        return user;
    }

    public String getUserName() {
        return userName;
    }

    public Role getRole() {
        return role;
    }

    public String getRoleName() {
        return roleName;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public Long getApplicationId() {
        return this.applicationId;
    }

    public void setUser(Long user) {
        this.user = user;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public ProcessRoleResource setUserEmail(String userEmail) {
        this.userEmail = userEmail;
        return this;
    }
}
