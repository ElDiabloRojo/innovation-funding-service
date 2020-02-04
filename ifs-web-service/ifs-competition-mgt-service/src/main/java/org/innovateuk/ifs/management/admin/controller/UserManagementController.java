package org.innovateuk.ifs.management.admin.controller;

import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.invite.resource.EditUserResource;
import org.innovateuk.ifs.invite.service.InviteUserRestService;
import org.innovateuk.ifs.management.admin.form.ConfirmEmailForm;
import org.innovateuk.ifs.management.admin.form.EditUserForm;
import org.innovateuk.ifs.management.admin.form.EditUserForm.InternalUserFieldsGroup;
import org.innovateuk.ifs.management.admin.viewmodel.ConfirmEmailViewModel;
import org.innovateuk.ifs.management.admin.viewmodel.ViewUserViewModel;
import org.innovateuk.ifs.management.registration.service.InternalUserService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.resource.UserStatus;
import org.innovateuk.ifs.user.service.RoleProfileStatusRestService;
import org.innovateuk.ifs.user.service.UserRestService;
import org.innovateuk.ifs.util.EncryptedCookieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.Validator;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.asGlobalErrors;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.fieldErrorsToFieldErrors;
import static org.innovateuk.ifs.user.resource.Role.IFS_ADMINISTRATOR;

/**
 * This controller will handle all requests that are related to management of users by IFS Administrators.
 */
@Controller
@RequestMapping("/admin/user/{userId}")
public class UserManagementController {

    private static final String FORM_ATTR_NAME = "form";
    private static final String NEW_EMAIL_COOKIE = "NEW_EMAIL_COOKIE";

    @Autowired
    private UserRestService userRestService;

    @Autowired
    private InviteUserRestService inviteUserRestService;

    @Autowired
    private InternalUserService internalUserService;

    @Autowired
    private Validator validator;

    @Autowired
    private EncryptedCookieService cookieService;

    @Autowired
    private RoleProfileStatusRestService roleProfileStatusRestService;

    @Value("${ifs.assessor.profile.feature.toggle}")
    private boolean profileFeatureToggle;

    @PreAuthorize("hasAnyAuthority('ifs_administrator', 'support')")
    @SecuredBySpring(value = "UserManagementController.viewUser() method",
            description = "IFS admins and support users can view users.")
    @GetMapping
    public String viewUser(@PathVariable long userId) {
        return userRestService.retrieveUserById(userId).andOnSuccessReturn(user -> {
            if (UserStatus.ACTIVE.equals(user.getStatus())) {
                return redirectToActivePage(userId);
            } else {
                return redirectToInactivePage(userId);
            }
        }).getSuccess();
    }

    @PreAuthorize("hasPermission(#userId, 'org.innovateuk.ifs.user.resource.UserCompositeId', 'VIEW_USER_PAGE')")
    @GetMapping("/inactive")
    public String viewInactiveUser(@PathVariable long userId, Model model, UserResource loggedInUser) {
        return userRestService.retrieveUserById(userId).andOnSuccessReturn(user -> {
            model.addAttribute("model", populateEditUserViewModel(user, loggedInUser));
            return "admin/inactive-user";
        }).getSuccess();
    }

    @PreAuthorize("hasPermission(#userId, 'org.innovateuk.ifs.user.resource.UserCompositeId', 'VIEW_USER_PAGE')")
    @GetMapping("/active")
    public String viewActiveUser(@PathVariable long userId, Model model, UserResource loggedInUser) {
        UserResource user = userRestService.retrieveUserById(userId).getSuccess();
        model.addAttribute(FORM_ATTR_NAME, populateForm(user));
        return viewActiveUser(model, user, loggedInUser);
    }

    @PreAuthorize("hasAnyAuthority('ifs_administrator', 'support')")
    @SecuredBySpring(value = "UserManagementController.updateUser() method",
            description = "IFS admins and support users can edit users.")
    @PostMapping("/active")
    public String updateUser(@PathVariable long userId,
                             Model model,
                             UserResource loggedInUser,
                             @Valid @ModelAttribute(FORM_ATTR_NAME) EditUserForm form,
                             @SuppressWarnings("unused") BindingResult bindingResult,
                             ValidationHandler validationHandler,
                             HttpServletResponse response) {
        UserResource user = userRestService.retrieveUserById(userId).getSuccess();
        validateUser(form, user);

        Supplier<String> failureView = () -> viewActiveUser(model, user, loggedInUser);
        Supplier<String> noEmailChangeSuccess = () -> redirectToActiveUsersTab();
        Supplier<String> emailChangeSuccess = () -> {
            cookieService.saveToCookie(response, NEW_EMAIL_COOKIE, form.getEmail());
            return String.format("redirect:/admin/user/%d/active/confirm", userId);
        };
        Supplier<String> successView = !user.getEmail().equals(form.getEmail()) ? emailChangeSuccess : noEmailChangeSuccess;

        return validationHandler.failNowOrSucceedWith(failureView, () -> {
            ServiceResult<Void> saveResult = serviceSuccess();
            if (user.isInternalUser()) {
                saveResult = internalUserService.editInternalUser(constructEditUserResource(form, userId));
            }
            return validationHandler.addAnyErrors(saveResult, fieldErrorsToFieldErrors(), asGlobalErrors()).
                    failNowOrSucceedWith(failureView, successView);
        });
    }

    @PreAuthorize("hasAnyAuthority('ifs_administrator', 'support')")
    @SecuredBySpring(value = "UserManagementController.confirmEmailChange() method",
            description = "IFS admins and support users can confirm email change.")
    @GetMapping("/active/confirm")
    public String confirmEmailChange(@PathVariable long userId,
                                     Model model,
                                     @ModelAttribute(value = FORM_ATTR_NAME, binding = false) ConfirmEmailForm form,
                                     @SuppressWarnings("unused") BindingResult bindingResult,
                                     HttpServletRequest request) {
        String email = cookieService.getCookieValue(request, NEW_EMAIL_COOKIE);
        if (email.isEmpty()) {
            return redirectToActivePage(userId);
        }
        UserResource user = userRestService.retrieveUserById(userId).getSuccess();
        model.addAttribute("model", new ConfirmEmailViewModel(user, email));
        return "admin/confirm-email";
    }

    @PreAuthorize("hasAnyAuthority('ifs_administrator', 'support')")
    @SecuredBySpring(value = "UserManagementController.confirmEmailChange() method",
            description = "IFS admins and support users can confirm email change.")
    @PostMapping("/active/confirm")
    public String confirmEmailChangePost(@PathVariable long userId,
                                         Model model,
                                         @Valid @ModelAttribute(value = FORM_ATTR_NAME) ConfirmEmailForm form,
                                         @SuppressWarnings("unused") BindingResult bindingResult,
                                         ValidationHandler validationHandler,
                                         HttpServletRequest request,
                                         HttpServletResponse response,
                                         RedirectAttributes redirectAttributes) {
        Supplier<String> failureView = () -> confirmEmailChange(userId, model, form, bindingResult, request);
        Supplier<String> successView = () -> {
            cookieService.removeCookie(response, NEW_EMAIL_COOKIE);
            redirectAttributes.addFlashAttribute("showEmailUpdateSuccess", true);
            return redirectToActiveUsersTab();
        };

        return validationHandler.failNowOrSucceedWith(failureView, () -> {
            String email = cookieService.getCookieValue(request, NEW_EMAIL_COOKIE);
            if (email.isEmpty()) {
                return redirectToActivePage(userId);
            }
            validationHandler.addAnyErrors(userRestService.updateEmail(userId, email));
            return validationHandler.failNowOrSucceedWith(failureView, successView);
        });
    }

    @PreAuthorize("hasAnyAuthority('ifs_administrator', 'support')")
    @SecuredBySpring(value = "UserManagementController.deactivateUser() method",
            description = "IFS admins and support users can deactivate users.")
    @PostMapping(value = "/active", params = "deactivateUser")
    public String deactivateUser(@PathVariable long userId) {
        return userRestService.retrieveUserById(userId).andOnSuccess(user ->
                userRestService.deactivateUser(userId)
                        .andOnSuccessReturn(p -> redirectToInactivePage(userId)))
                .getSuccess();
    }

    @PreAuthorize("hasAnyAuthority('ifs_administrator', 'support')")
    @SecuredBySpring(value = "UserManagementController.reactivateUser() method",
            description = "IFS admins and support users can reactivate users.")
    @PostMapping(value = "/inactive", params = "reactivateUser")
    public String reactivateUser(@PathVariable long userId) {
        return userRestService.retrieveUserById(userId).andOnSuccess(user ->
                userRestService.reactivateUser(userId)
                        .andOnSuccessReturn(p -> redirectToActivePage(userId)))
                .getSuccess();
    }

    private EditUserForm populateForm(UserResource user) {
        EditUserForm form = new EditUserForm();
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());

        if (user.getRoles().contains(IFS_ADMINISTRATOR)) {
            form.setRole(IFS_ADMINISTRATOR);
        } else {
            form.setRole(user.getRoles().stream().findFirst().get());
        }
        form.setEmail(user.getEmail());
        return form;
    }

    private String viewActiveUser(Model model, UserResource user, UserResource loggedInUser) {
        model.addAttribute("model", populateEditUserViewModel(user, loggedInUser));
        return "admin/active-user";
    }

    private void validateUser(EditUserForm form, UserResource user) {
        if (user.isInternalUser()) {
            validator.validate(form, InternalUserFieldsGroup.class);
        }
    }

    private static EditUserResource constructEditUserResource(EditUserForm form, long userId) {
        return new EditUserResource(userId, form.getFirstName(), form.getLastName(), form.getRole());
    }

    private ViewUserViewModel populateEditUserViewModel(UserResource user, UserResource loggedInUser) {
        return new ViewUserViewModel(user,
                loggedInUser,
                roleProfileStatusRestService.findByUserId(user.getId())
                        .getOptionalSuccessObject()
                        .orElse(emptyList()),
                profileFeatureToggle);
    }

    private String redirectToActiveUsersTab() {
        return "redirect:/admin/users/active";
    }

    private String redirectToActivePage(long userId) {
        return String.format("redirect:/admin/user/%d/active", userId);
    }

    private String redirectToInactivePage(long userId) {
        return String.format("redirect:/admin/user/%d/inactive", userId);
    }
}