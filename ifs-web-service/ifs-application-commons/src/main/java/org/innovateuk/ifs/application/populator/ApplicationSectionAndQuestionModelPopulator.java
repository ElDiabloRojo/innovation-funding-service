package org.innovateuk.ifs.application.populator;

import org.innovateuk.ifs.applicant.service.ApplicantRestService;
import org.innovateuk.ifs.application.populator.forminput.FormInputViewModelGenerator;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.FormInputResponseResource;
import org.innovateuk.ifs.application.resource.QuestionStatusResource;
import org.innovateuk.ifs.application.service.QuestionService;
import org.innovateuk.ifs.application.service.SectionService;
import org.innovateuk.ifs.application.viewmodel.forminput.AbstractFormInputViewModel;
import org.innovateuk.ifs.category.service.CategoryRestService;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.form.ApplicationForm;
import org.innovateuk.ifs.form.Form;
import org.innovateuk.ifs.form.resource.FormInputResource;
import org.innovateuk.ifs.form.resource.QuestionResource;
import org.innovateuk.ifs.form.resource.SectionResource;
import org.innovateuk.ifs.form.resource.SectionType;
import org.innovateuk.ifs.form.service.FormInputResponseRestService;
import org.innovateuk.ifs.form.service.FormInputResponseService;
import org.innovateuk.ifs.form.service.FormInputRestService;
import org.innovateuk.ifs.invite.resource.ApplicationInviteResource;
import org.innovateuk.ifs.invite.InviteService;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.ProcessRoleService;
import org.innovateuk.ifs.user.service.UserService;
import org.innovateuk.ifs.util.CollectionFunctions;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.innovateuk.ifs.form.resource.FormInputScope.APPLICATION;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFilter;

@Component
public class ApplicationSectionAndQuestionModelPopulator {
    public static final String MODEL_ATTRIBUTE_FORM = "form";

    protected FormInputRestService formInputRestService;
    protected FormInputResponseService formInputResponseService;
    protected FormInputResponseRestService formInputResponseRestService;
    protected QuestionService questionService;
    protected ProcessRoleService processRoleService;
    protected SectionService sectionService;
    private CategoryRestService categoryRestService;
    private ApplicantRestService applicantRestService;
    private UserService userService;
    private FormInputViewModelGenerator formInputViewModelGenerator;
    private InviteService inviteService;

    public ApplicationSectionAndQuestionModelPopulator(FormInputRestService formInputRestService,
                                                       FormInputResponseService formInputResponseService,
                                                       FormInputResponseRestService formInputResponseRestService,
                                                       QuestionService questionService,
                                                       ProcessRoleService processRoleService,
                                                       SectionService sectionService,
                                                       CategoryRestService categoryRestService,
                                                       ApplicantRestService applicantRestService,
                                                       UserService userService,
                                                       FormInputViewModelGenerator formInputViewModelGenerator,
                                                       InviteService inviteService) {
        this.formInputRestService = formInputRestService;
        this.formInputResponseService = formInputResponseService;
        this.formInputResponseRestService = formInputResponseRestService;
        this.questionService = questionService;
        this.processRoleService = processRoleService;
        this.sectionService = sectionService;
        this.categoryRestService = categoryRestService;
        this.applicantRestService = applicantRestService;
        this.userService = userService;
        this.formInputViewModelGenerator = formInputViewModelGenerator;
        this.inviteService = inviteService;
    }

    public void addMappedSectionsDetails(Model model, ApplicationResource application, CompetitionResource competition,
                                         Optional<SectionResource> currentSection,
                                         Optional<OrganisationResource> userOrganisation,
                                         Long userId,
                                         Map<Long, Set<Long>> completedSectionsByOrganisation,
                                         Optional<Boolean> markAsCompleteEnabled) {

        List<SectionResource> allSections = sectionService.getAllByCompetitionId(competition.getId());
        List<SectionResource> parentSections = sectionService.filterParentSections(allSections);

        Map<Long, SectionResource> sections =
                parentSections.stream().collect(CollectionFunctions.toLinkedMap(SectionResource::getId,
                        Function.identity()));

        userOrganisation.ifPresent(org -> {
            Set<Long> completedSectionsForThisOrganisation = completedSectionsByOrganisation.get(userOrganisation);
            model.addAttribute("completedSections", completedSectionsForThisOrganisation);
        });

        List<QuestionResource> questions = questionService.findByCompetition(competition.getId());
        markAsCompleteEnabled.ifPresent(markAsCompleteEnabledBoolean ->
            questions.forEach(questionResource -> questionResource.setMarkAsCompletedEnabled(markAsCompleteEnabledBoolean))
        );

        List<FormInputResource> formInputResources = formInputRestService.getByCompetitionIdAndScope(
                competition.getId(), APPLICATION).getSuccess();

        model.addAttribute("sections", sections);

        Optional<SectionResource> financeSection = allSections.stream()
                .filter(section -> section.getType() == SectionType.FUNDING_FINANCES)
                .findFirst();
        model.addAttribute("fundingFinancesSection", financeSection.orElse(null));

        Map<Long, List<QuestionResource>> sectionQuestions = parentSections.stream()
                .collect(Collectors.toMap(
                        SectionResource::getId,
                        s -> getQuestionsBySection(s.getQuestions(), questions)
                ));
        Map<Long, List<FormInputResource>> questionFormInputs = sectionQuestions.values().stream()
                .flatMap(a -> a.stream())
                .collect(Collectors.toMap(q -> q.getId(), k -> findFormInputByQuestion(k.getId(), formInputResources)));
        model.addAttribute("questionFormInputs", questionFormInputs);
        model.addAttribute("sectionQuestions", sectionQuestions);


        //Comp admin user doesn't have user organisation
        long applicantId;
        if (!userOrganisation.isPresent())  {
            ProcessRoleResource leadApplicantProcessRole = userService.getLeadApplicantProcessRoleOrNull(application.getId());
            applicantId = leadApplicantProcessRole.getUser();
        } else {
            applicantId = userId;
        }
        Map<Long, AbstractFormInputViewModel> formInputViewModels = sectionQuestions.values().stream().flatMap(List::stream)
                .map(question -> applicantRestService.getQuestion(applicantId, application.getId(), question.getId()))
                .map(applicationQuestion -> formInputViewModelGenerator.fromQuestion(applicationQuestion, new ApplicationForm()))
                .flatMap(List::stream)
                .collect(Collectors.toMap(viewModel -> viewModel.getFormInput().getId(), Function.identity()));
        model.addAttribute("formInputViewModels", formInputViewModels);
        formInputViewModels.values().forEach(viewModel -> {
            viewModel.setClosed(!(competition.isOpen() && application.isOpen()));
            viewModel.setReadonly(true);
            viewModel.setSummary(true);
        });


        addSubSections(currentSection, model, parentSections, allSections, questions, formInputResources);
    }

    public void addSectionDetails(Model model, Optional<SectionResource> currentSection) {
        model.addAttribute("currentSectionId", currentSection.map(SectionResource::getId).orElse(null));
        model.addAttribute("currentSection", currentSection.orElse(null));
        if (currentSection.isPresent()) {
            List<QuestionResource> questions = getQuestionsBySection(currentSection.get().getQuestions(), questionService.findByCompetition(currentSection.get().getCompetition()));
            Map<Long, List<QuestionResource>> sectionQuestions = new HashMap<>();
            sectionQuestions.put(currentSection.get().getId(), questions);
            Map<Long, List<FormInputResource>> questionFormInputs = sectionQuestions.values().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toMap(QuestionResource::getId, question ->
                            formInputRestService.getByQuestionIdAndScope(question.getId(), APPLICATION).getSuccess()));

            model.addAttribute("questionFormInputs", questionFormInputs);
            model.addAttribute("sectionQuestions", sectionQuestions);
            model.addAttribute("title", currentSection.get().getName());
        }
    }

    public void addQuestionsDetails(Model model, ApplicationResource application, Form form) {
        List<FormInputResponseResource> responses = getFormInputResponses(application);
        Map<Long, FormInputResponseResource> mappedResponses = formInputResponseService.mapFormInputResponsesToFormInput(responses);
        model.addAttribute("responses",mappedResponses);

        if(form == null){
            form = new Form();
        }
        Map<String, String> values = form.getFormInput();
        mappedResponses.forEach((k, v) ->
                values.put(k.toString(), v.getValue())
        );
        form.setFormInput(values);
        model.addAttribute(MODEL_ATTRIBUTE_FORM, form);
    }

    public void addAssignableDetails(Model model, ApplicationResource application, OrganisationResource userOrganisation,
                                     UserResource user, Optional<SectionResource> currentSection, Optional<Long> currentQuestionId) {

        if (isApplicationInViewMode(model, application, userOrganisation)) {
            return;
        }

        Map<Long, QuestionStatusResource> questionAssignees = getQuestionAssignees(currentSection, currentQuestionId, application, userOrganisation);
        if (currentQuestionId.isPresent()) {
            QuestionStatusResource questionAssignee = questionAssignees.get(currentQuestionId.get());
            model.addAttribute("questionAssignee", questionAssignee);
        }
        List<QuestionStatusResource> notifications = questionService.getNotificationsForUser(questionAssignees.values(), user.getId());
        questionService.removeNotifications(notifications);
        List<ApplicationInviteResource> pendingAssignableUsers = inviteService.getPendingInvitationsByApplicationId(application.getId());

        model.addAttribute("assignableUsers", processRoleService.findAssignableProcessRoles(application.getId()));
        model.addAttribute("pendingAssignableUsers", pendingAssignableUsers);
        model.addAttribute("questionAssignees", questionAssignees);
        model.addAttribute("notifications", notifications);
    }

    public void addCompletedDetails(Model model, ApplicationResource application, Optional<OrganisationResource> userOrganisation, Map<Long, Set<Long>> completedSectionsByOrganisation) {

        Future<Set<Long>> markedAsCompleteQuestions = getMarkedAsCompleteDetails(application, userOrganisation); // List of question ids
        model.addAttribute("markedAsComplete", markedAsCompleteQuestions);

        Set<Long> sectionsMarkedAsComplete = getCompletedSectionsForUserOrganisation(completedSectionsByOrganisation, userOrganisation);

        model.addAttribute("completedSectionsByOrganisation", completedSectionsByOrganisation);
        model.addAttribute("sectionsMarkedAsComplete", sectionsMarkedAsComplete);
        model.addAttribute("allQuestionsCompleted", sectionService.allSectionsMarkedAsComplete(application.getId()));
        model.addAttribute("researchCategories", categoryRestService.getResearchCategories().getSuccess());

        addFinanceDetails(model, application);

        List<SectionResource> eachOrganisationFinanceSections = sectionService.getSectionsForCompetitionByType(application.getCompetition(), SectionType.FINANCE);
        Long eachCollaboratorFinanceSectionId = getEachCollaboratorFinanceSectionId(eachOrganisationFinanceSections);

        model.addAttribute("eachCollaboratorFinanceSectionId", eachCollaboratorFinanceSectionId);
    }

    private Long getEachCollaboratorFinanceSectionId(List<SectionResource> eachOrganisationFinanceSections) {
        if (!eachOrganisationFinanceSections.isEmpty()) {
            return eachOrganisationFinanceSections.get(0).getId();
        }
        
        return null;
    }

    private Set<Long> getCompletedSectionsForUserOrganisation(Map<Long, Set<Long>> completedSectionsByOrganisation, Optional<OrganisationResource> userOrganisation) {
        return completedSectionsByOrganisation.getOrDefault(
                userOrganisation.map(OrganisationResource::getId)
                        .orElse(-1L),
                new HashSet<>()
        );
    }

    public Optional<SectionResource> getSectionByIds(Long competitionId, Optional<Long> sectionId, boolean selectFirstSectionIfNoneCurrentlySelected) {
        List<SectionResource> allSections = sectionService.getAllByCompetitionId(competitionId);
        return getSection(allSections, sectionId, selectFirstSectionIfNoneCurrentlySelected);
    }

    private List<QuestionResource> getQuestionsBySection(final List<Long> questionIds, final List<QuestionResource> questions) {
        return questions.stream()
                .filter(q -> questionIds.contains(q.getId()))
                .sorted()
                .collect(toList());
    }

    private Optional<SectionResource> getSection(List<SectionResource> sections, Optional<Long> sectionId, boolean selectFirstSectionIfNoneCurrentlySelected) {
        if (sectionId.isPresent()) {
            Long id = sectionId.get();
            // get the section that we want to show, so we can use this on to show the correct questions.
            return sections.stream().filter(x -> x.getId().equals(id)).findFirst();

        } else if (selectFirstSectionIfNoneCurrentlySelected) {
            return sections.isEmpty() ? Optional.empty() : Optional.ofNullable(sections.get(0));
        }
        return Optional.empty();
    }

    private List<SectionResource> getSectionsFromListByIdList(final List<Long> childSections, final List<SectionResource> allSections) {
        return simpleFilter(allSections, section -> childSections.contains(section.getId()));
    }

    private List<FormInputResponseResource> getFormInputResponses(ApplicationResource application) {
        return formInputResponseRestService.getResponsesByApplicationId(application.getId()).getSuccess();
    }

    private Future<Set<Long>> getMarkedAsCompleteDetails(ApplicationResource application, Optional<OrganisationResource> userOrganisation) {
        Long organisationId = userOrganisation.orElseGet(() -> {
            OrganisationResource organisation = new OrganisationResource();
            organisation.setId(0L);
            return organisation;
        }).getId();

        return questionService.getMarkedAsComplete(application.getId(), organisationId);
    }

    private boolean isApplicationInViewMode(Model model, ApplicationResource application, OrganisationResource userOrganisation) {
        if(!application.isOpen() || userOrganisation == null){
            //Application Not open, so add empty lists
            model.addAttribute("assignableUsers", new ArrayList<ProcessRoleResource>());
            model.addAttribute("pendingAssignableUsers", new ArrayList<ApplicationInviteResource>());
            model.addAttribute("questionAssignees", new HashMap<Long, QuestionStatusResource>());
            model.addAttribute("notifications", new ArrayList<QuestionStatusResource>());
            return true;
        }
        return false;
    }

    private List<FormInputResource> findFormInputByQuestion(final Long id, final List<FormInputResource> list) {
        return simpleFilter(list, input -> input.getQuestion().equals(id));
    }

    private void addSubSections(Optional<SectionResource> currentSection, Model model, List<SectionResource> parentSections,
                                List<SectionResource> allSections, List<QuestionResource> questions, List<FormInputResource> formInputResources) {
        Map<Long, List<QuestionResource>> subsectionQuestions;
        if (currentSection.isPresent()) {
            Map<Long, List<SectionResource>>  subSections = new HashMap<>();
            subSections.put(currentSection.get().getId(), getSectionsFromListByIdList(currentSection.get().getChildSections(), allSections));

            model.addAttribute("subSections", subSections);
            subsectionQuestions = subSections.get(currentSection.get().getId()).stream()
                    .collect(Collectors.toMap(SectionResource::getId,
                            ss -> getQuestionsBySection(ss.getQuestions(), questions)
                    ));
            model.addAttribute("subsectionQuestions", subsectionQuestions);
        } else {
            Map<Long, List<SectionResource>>   subSections = parentSections.stream()
                    .collect(Collectors.toMap(
                            SectionResource::getId, s -> getSectionsFromListByIdList(s.getChildSections(), allSections)
                    ));
            model.addAttribute("subSections", subSections);
            subsectionQuestions = parentSections.stream()
                    .collect(Collectors.toMap(SectionResource::getId,
                            ss -> getQuestionsBySection(ss.getQuestions(), questions)
                    ));
            model.addAttribute("subsectionQuestions", subsectionQuestions);
        }

        Map<Long, List<FormInputResource>> subSectionQuestionFormInputs = subsectionQuestions.values().stream().flatMap(a -> a.stream()).collect(Collectors.toMap(q -> q.getId(), k -> findFormInputByQuestion(k.getId(), formInputResources)));
        model.addAttribute("subSectionQuestionFormInputs", subSectionQuestionFormInputs);
    }

    private Map<Long, QuestionStatusResource> getQuestionAssignees(Optional<SectionResource> currentSection,
                                                                   Optional<Long> currentQuestionId,
                                                                   ApplicationResource application,
                                                                   OrganisationResource userOrganisation) {
        Map<Long, QuestionStatusResource> questionAssignees;
        if (currentQuestionId.isPresent()) {
            QuestionStatusResource questionStatusResource = questionService.getByQuestionIdAndApplicationIdAndOrganisationId(currentQuestionId.get(), application.getId(), userOrganisation.getId());
            questionAssignees = new HashMap<>();
            if (questionStatusResource != null) {
                questionAssignees.put(currentQuestionId.get(), questionStatusResource);
            }
        } else if (currentSection.isPresent()) {
            SectionResource section = currentSection.get();
            questionAssignees = questionService.getQuestionStatusesByQuestionIdsAndApplicationIdAndOrganisationId(section.getQuestions(), application.getId(), userOrganisation.getId());
        } else {
            questionAssignees = questionService.getQuestionStatusesForApplicationAndOrganisation(application.getId(), userOrganisation.getId());
        }
        return questionAssignees;
    }

    private void addFinanceDetails(Model model, ApplicationResource application) {
        SectionResource financeSection = sectionService.getFinanceSection(application.getCompetition());
        final boolean hasFinanceSection;
        final Long financeSectionId;
        if (financeSection == null) {
            hasFinanceSection = false;
            financeSectionId = null;
        } else {
            hasFinanceSection = true;
            financeSectionId = financeSection.getId();
        }

        model.addAttribute("hasFinanceSection", hasFinanceSection);
        model.addAttribute("financeSectionId", financeSectionId);
    }
}
