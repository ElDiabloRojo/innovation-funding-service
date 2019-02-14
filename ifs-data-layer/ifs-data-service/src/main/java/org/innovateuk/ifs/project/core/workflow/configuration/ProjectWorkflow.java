package org.innovateuk.ifs.project.core.workflow.configuration;

import org.innovateuk.ifs.project.resource.ProjectEvent;
import org.innovateuk.ifs.project.resource.ProjectState;
import org.innovateuk.ifs.workflow.WorkflowStateMachineListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.LinkedHashSet;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.project.resource.ProjectEvent.*;
import static org.innovateuk.ifs.project.resource.ProjectState.*;

/**
 * Describes the workflow for Project Setup.
 */
@Configuration
@EnableStateMachineFactory(name = "projectStateMachineFactory")
public class ProjectWorkflow extends StateMachineConfigurerAdapter<ProjectState, ProjectEvent> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<ProjectState, ProjectEvent> config) throws Exception {
        config.withConfiguration().listener(new WorkflowStateMachineListener<>());

    }

    @Override
    public void configure(StateMachineStateConfigurer<ProjectState, ProjectEvent> states) throws Exception {
        states.withStates()
                .initial(SETUP)
                .states(new LinkedHashSet<>(asList(ProjectState.values())))
                .end(LIVE);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ProjectState, ProjectEvent> transitions) throws Exception {
        transitions
                .withExternal()
                    .source(SETUP)
                    .event(PROJECT_CREATED)
                    .target(SETUP)
                .and()
                .withExternal()
                    .source(SETUP)
                    .event(GOL_APPROVED)
                    .target(LIVE)
                .and()
                .withExternal()
                    .source(SETUP)
                    .event(PROJECT_WITHDRAWN)
                    .target(WITHDRAWN)
                .and()
                .withExternal()
                    .source(SETUP)
                    .event(HANDLE_OFFLINE)
                    .target(HANDLED_OFFLINE)
                .and()
                .withExternal()
                    .source(HANDLED_OFFLINE)
                    .event(COMPLETE_OFFLINE)
                    .target(COMPLETED_OFFLINE);
    }
}
