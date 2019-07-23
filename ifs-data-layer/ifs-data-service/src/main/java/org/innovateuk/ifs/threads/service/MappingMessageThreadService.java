package org.innovateuk.ifs.threads.service;

import org.innovateuk.ifs.commons.mapper.BaseMapper;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.threads.domain.MessageThread;
import org.innovateuk.ifs.threads.mapper.PostMapper;
import org.innovateuk.ifs.threads.repository.MessageThreadRepository;
import org.innovateuk.ifs.threads.resource.PostResource;
import org.innovateuk.ifs.util.AuthenticationHelper;

import java.util.List;

import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;

public class MappingMessageThreadService<D extends MessageThread, R, M extends BaseMapper<D, R, Long>, C> implements MessageThreadService<R, PostResource> {
    private final GenericMessageThreadService<D, C> service;
    private final M threadMapper;
    private final PostMapper postMapper;

    public MappingMessageThreadService(MessageThreadRepository<D> threadRepository, AuthenticationHelper authenticationHelper, M threadMapper, PostMapper postMapper, Class<C> context) {
        this.service = new GenericMessageThreadService<>(threadRepository, authenticationHelper, context);
        this.threadMapper = threadMapper;
        this.postMapper = postMapper;
    }

    public ServiceResult<List<R>> findAll(Long contextClassId) {
        return service.findAll(contextClassId)
                .andOnSuccessReturn(queries -> simpleMap(queries, threadMapper::mapToResource));
    }

    public ServiceResult<R> findOne(Long id) {
        return service.findOne(id).andOnSuccessReturn(threadMapper::mapToResource);
    }

    public ServiceResult<Long> create(R query) {
        return service.create(threadMapper.mapToDomain(query));
    }

    @Override
    public ServiceResult<Void> close(Long threadId) {
        return service.close(threadId);
    }

    public ServiceResult<Void> addPost(PostResource post, Long threadId) {
        return service.addPost(postMapper.mapToDomain(post), threadId);
    }
}