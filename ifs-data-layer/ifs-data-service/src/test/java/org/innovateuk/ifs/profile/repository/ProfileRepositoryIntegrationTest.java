package org.innovateuk.ifs.profile.repository;

import org.innovateuk.ifs.BaseRepositoryIntegrationTest;
import org.innovateuk.ifs.category.domain.InnovationArea;
import org.innovateuk.ifs.category.repository.InnovationAreaRepository;
import org.innovateuk.ifs.profile.domain.Profile;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.innovateuk.ifs.profile.builder.ProfileBuilder.newProfile;
import static org.junit.Assert.assertTrue;

public class ProfileRepositoryIntegrationTest extends BaseRepositoryIntegrationTest<ProfileRepository> {

    @Override
    @Autowired
    protected void setRepository(ProfileRepository repository) {
        this.repository = repository;
    }

    @Autowired
    private InnovationAreaRepository innovationAreaRepository;

    @Test
    public void saveWithInnovationArea() {
        loginPaulPlum();

        // avoid Ids clashing with existing data
        final Long profileId[] = {0L};
        repository.findAll().forEach(p -> {
            profileId[0] = new Long(Math.max(p.getId().longValue(), profileId[0].longValue()));
        });

        InnovationArea innovationArea = innovationAreaRepository.findByName("Emerging technology");
        Profile profile = newProfile().withId(profileId[0] + 1).build();
        Profile savedProfile = repository.save(profile);
        savedProfile.addInnovationArea(innovationArea);
        savedProfile = repository.save(savedProfile);

        flushAndClearSession();

        Profile retrievedProfile = repository.findOne(savedProfile.getId());

        assertTrue(retrievedProfile.getInnovationAreas().contains(innovationArea));
    }
}
