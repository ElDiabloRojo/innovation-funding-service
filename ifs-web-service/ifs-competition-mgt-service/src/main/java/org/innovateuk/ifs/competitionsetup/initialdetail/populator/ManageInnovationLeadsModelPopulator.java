package org.innovateuk.ifs.competitionsetup.initialdetail.populator;

import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.competitionsetup.initialdetail.viewmodel.ManageInnovationLeadsViewModel;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.user.resource.Role.INNOVATION_LEAD;

@Service
public class ManageInnovationLeadsModelPopulator {

    @Autowired
    private CompetitionRestService competitionRestService;

    @Autowired
    private UserService userService;

    public ManageInnovationLeadsViewModel populateModel(CompetitionResource competition) {

        List<UserResource> availableInnovationLeads = userService.findUserByType(INNOVATION_LEAD);
        List<UserResource> innovationLeadsAssignedToCompetition = competitionRestService.findInnovationLeads(competition.getId()).getSuccess();
        availableInnovationLeads.removeAll(innovationLeadsAssignedToCompetition);

        UserResource leadTechnologistAssignedToCompetition = userService.findById(competition.getLeadTechnologist());
        availableInnovationLeads.remove(leadTechnologistAssignedToCompetition);
        innovationLeadsAssignedToCompetition.remove(leadTechnologistAssignedToCompetition);

        return new ManageInnovationLeadsViewModel(competition.getId(), competition.getName(),
                competition.getLeadTechnologistName(), competition.getExecutiveName(), competition.getInnovationSectorName(),
                competition.getInnovationAreaNames(), sortByName(innovationLeadsAssignedToCompetition),
                sortByName(availableInnovationLeads));

    }

    private List<UserResource> sortByName(List<UserResource> userResources) {
        return userResources.stream().sorted(Comparator.comparing(userResource -> userResource.getName().toUpperCase())).collect(Collectors.toList());
    }
}
