package org.innovateuk.ifs.threads.domain;

import org.innovateuk.ifs.user.domain.User;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.*;

@Entity
@Table(name = "thread")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "thread_type", discriminatorType = DiscriminatorType.STRING)
public abstract class MessageThread {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private Long classPk;
    @NotNull
    private String className;

    @Size(max = 255)
    private String title;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "thread_id", referencedColumnName = "id", nullable = false)
    @OrderBy("created_on ASC")
    private List<Post> posts;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdOn;

    private ZonedDateTime closedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closed_by_user_id", referencedColumnName = "id")
    private User closedBy;

    MessageThread() {
    }

    MessageThread(Long id, Long classPk, String className, List<Post> posts, String title, ZonedDateTime createdOn) {
        this.id = id;
        this.classPk = classPk;
        this.className = className;
        this.posts = ofNullable(posts).map(ArrayList::new).orElse(new ArrayList<>());
        this.title = title;
        this.createdOn = createdOn;
    }

    public final Optional<Post> latestPost() {
        return postAtIndex(posts.size() - 1);
    }

    private final Optional<Post> postAtIndex(int index) {
        return indexWithinBounds(index) ? of(posts.get(index)) : empty();
    }

    private boolean indexWithinBounds(int index) {
        return index >= 0 && index < posts.size();
    }

    public List<Post> posts() {
        return new ArrayList<>(posts);
    }

    public void addPost(Post post) {
        posts.add(post);
    }

    public Long id() {
        return id;
    }

    public Long contextClassPk() {
        return classPk;
    }

    public String contextClassName() {
        return className;
    }
    public String title() {
        return title;
    }

    public ZonedDateTime createdOn() {
        return createdOn;
    }

    public void setContext(String context) {
        this.className = context;
    }

    public ZonedDateTime getClosedDate() {
        return closedDate;
    }

    public User getClosedBy() {
        return closedBy;
    }

    public void closeThread(User closedBy) {
        this.closedBy = closedBy;
        this.closedDate = ZonedDateTime.now();
    }

    public void setClosedDate(ZonedDateTime closedDate) {
        this.closedDate = closedDate;
    }

    public void setClosedBy(User closedBy) {
        this.closedBy = closedBy;
    }
}