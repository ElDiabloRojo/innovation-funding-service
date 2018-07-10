package org.innovateuk.ifs.workflow.audit;

import org.innovateuk.ifs.workflow.domain.Process;

import javax.persistence.PreUpdate;

import static org.innovateuk.ifs.workflow.audit.ProcessHistoryRepositoryProvider.processHistoryRepository;

/**
 * {@code EntityListener} to create new {@link ProcessHistory}s whenever a {@link Process} is updated.
 */
public class ProcessHistoryEntityListener {

    @PreUpdate
    public void preUpdate(Process process) {
        try {
            processHistoryRepository().ifPresent(r -> r.save(new ProcessHistory(process)));
        }
        catch (IllegalStateException e) {
            // TODO log -- this is to workaround issues running unit tests in bamboo
        }
    }
}