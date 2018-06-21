package org.innovateuk.ifs.competitionsetup.transactional.template;

import org.innovateuk.ifs.competition.transactional.template.BaseChainedTemplatePersistor;
import org.innovateuk.ifs.form.domain.GuidanceRow;
import org.innovateuk.ifs.form.repository.GuidanceRowRepository;
import org.innovateuk.ifs.form.domain.FormInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Transactional component providing functions for persisting copies of GuidanceRows by their parent FormInput entity object.
 */
@Component
public class GuidanceRowTemplatePersistorImpl implements BaseChainedTemplatePersistor<List<GuidanceRow>, FormInput> {
    @Autowired
    private GuidanceRowRepository guidanceRowRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public List<GuidanceRow> persistByParentEntity(FormInput formInput) {
        return formInput.getGuidanceRows() == null ? Collections.emptyList() : formInput.getGuidanceRows().stream().map(createFormInputGuidanceRow(formInput)).collect(Collectors.toList());
    }

    private Function<GuidanceRow, GuidanceRow> createFormInputGuidanceRow(FormInput formInput) {
        return (GuidanceRow row) -> {
            entityManager.detach(row);
            row.setFormInput(formInput);
            row.setId(null);
            guidanceRowRepository.save(row);
            return row;
        };
    }

    @Transactional
    public void cleanForParentEntity(FormInput formInput) {
        List<GuidanceRow> scoreRows = formInput.getGuidanceRows();
        if(!scoreRows.isEmpty()) {
            scoreRows.forEach(scoreRow -> entityManager.detach(scoreRow));
            guidanceRowRepository.deleteAll(scoreRows);
        }
    }
}
