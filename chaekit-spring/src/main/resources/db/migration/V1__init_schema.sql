CREATE TABLE member (
    id          bigint NOT NULL AUTO_INCREMENT,
    email       varchar(255) NOT NULL,
    password    varchar(255) NOT NULL,
    role        varchar(255) NOT NULL,
    created_at  datetime(6) DEFAULT NULL,
    modified_at datetime(6) DEFAULT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY `UK_member_email` (email)
);

CREATE TABLE user_profile (
    id              bigint NOT NULL AUTO_INCREMENT,
    member_id       bigint NOT NULL,
    nickname        varchar(255) NOT NULL,
    created_at      datetime(6) DEFAULT NULL,
    modified_at     datetime(6) DEFAULT NULL,
    profile_image_key varchar(255) NULL,

    PRIMARY KEY (id),
    CONSTRAINT FK_user_profile_member_id FOREIGN KEY (member_id) REFERENCES member (id)
);

CREATE TABLE ebook (
    id              bigint AUTO_INCREMENT,
    title           varchar(255) NOT NULL,
    author          varchar(255) NULL,
    description     varchar(10000) NULL,
    size            bigint NOT NULL,
    file_key        varchar(255) NOT NULL,
    cover_image_key varchar(255) NULL,
    view_count      bigint DEFAULT 0 NOT NULL,
    created_at      datetime(6) DEFAULT NULL,
    modified_at     datetime(6) DEFAULT NULL,

    PRIMARY KEY (id)
);
CREATE TABLE ebook_shelf_item (
    id          bigint AUTO_INCREMENT,
    book_id     bigint NULL,
    user_id     bigint NULL,
    cfi         varchar(255) NULL,
    percentage  bigint NOT NULL,
    created_at  datetime(6) DEFAULT NULL,
    modified_at datetime(6) DEFAULT NULL,

    PRIMARY KEY (id),
    CONSTRAINT FK_ebook_shelf_item_book_id FOREIGN KEY (book_id) REFERENCES ebook (id),
    CONSTRAINT FK_ebook_shelf_item_user_id FOREIGN KEY (user_id) REFERENCES user_profile (id)

);
CREATE TABLE reading_group (
    id               bigint AUTO_INCREMENT,
    name             varchar(255) NOT NULL,
    group_leader_id  bigint NOT NULL,
    description      varchar(5000) NULL,
    is_auto_approval bit DEFAULT b'1' NULL,
    group_image_key  varchar(255) NULL,
    created_at       datetime(6) DEFAULT NULL,
    modified_at      datetime(6) DEFAULT NULL,

    CONSTRAINT FK_reading_group_group_leader_id FOREIGN KEY (group_leader_id) REFERENCES user_profile (id),
    PRIMARY KEY (id)
);
CREATE TABLE group_tag (
    id               bigint AUTO_INCREMENT,
    tag_name         varchar(255) NOT NULL,
    reading_group_id bigint NULL,
    created_at       datetime(6) DEFAULT NULL,
    modified_at      datetime(6) DEFAULT NULL,
    CONSTRAINT FK_group_tag_reading_group_id FOREIGN KEY (reading_group_id) REFERENCES reading_group (id),
    PRIMARY KEY (id)
);
CREATE TABLE group_member (
    id          bigint AUTO_INCREMENT,
    group_id    bigint NOT NULL,
    user_id     bigint NOT NULL,
    is_accepted tinyint(1) DEFAULT 0 NULL,
    created_at  datetime(6) DEFAULT NULL,
    modified_at datetime(6) DEFAULT NULL,
    approved_at datetime(6) DEFAULT NULL,
    CONSTRAINT FK_group_member_group_id FOREIGN KEY (group_id) REFERENCES reading_group (id),
    CONSTRAINT FK_group_member_user_id FOREIGN KEY (user_id) REFERENCES user_profile (id),
    PRIMARY KEY (id)
);

CREATE TABLE group_chat (
    id         bigint AUTO_INCREMENT,
    content    varchar(1000) NOT NULL,
    author_id  bigint NULL,
    group_id   bigint NULL,
    CONSTRAINT FK_group_chat_author FOREIGN KEY (author_id) REFERENCES user_profile (id),
    CONSTRAINT FK_group_chat_group FOREIGN KEY (group_id) REFERENCES reading_group (id),
    created_at datetime(6) DEFAULT NULL,
    modified_at datetime(6) DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE activity (
    id          bigint AUTO_INCREMENT,
    group_id    bigint NULL,
    book_id     bigint NULL,
    description varchar(5000) NULL,
    start_time  date NOT NULL,
    end_time    date NOT NULL,
    created_at  datetime(6) DEFAULT NULL,
    modified_at datetime(6) DEFAULT NULL,

    CONSTRAINT FK_activity_group_id FOREIGN KEY (group_id) REFERENCES reading_group (id),
    CONSTRAINT FK_activity_book_id  FOREIGN KEY (book_id) REFERENCES ebook (id),
    PRIMARY KEY (id)
);

CREATE TABLE activity_member (
    id          bigint AUTO_INCREMENT,
    activity_id bigint NULL,
    user_id     bigint NULL,
    created_at  datetime(6) DEFAULT NULL,
    modified_at datetime(6) DEFAULT NULL,

    CONSTRAINT FK_activity_member_activity_id FOREIGN KEY (activity_id) REFERENCES activity (id),
    CONSTRAINT FK_activity_member_user_id     FOREIGN KEY (user_id) REFERENCES user_profile (id),
    PRIMARY KEY (id)
);

CREATE TABLE group_review (
                              id          bigint AUTO_INCREMENT,
                              group_id    bigint NOT NULL,
                              activity_id bigint NOT NULL,
                              author_id   bigint NOT NULL,
                              content     text NOT NULL,
                              CONSTRAINT FK_group_review_activity FOREIGN KEY (activity_id) REFERENCES activity (id),
                              CONSTRAINT FK_group_review_author FOREIGN KEY (author_id) REFERENCES user_profile (id),
                              created_at  datetime(6) DEFAULT NULL,
                              modified_at datetime(6) DEFAULT NULL,
                              PRIMARY KEY (id)
);

CREATE TABLE group_review_tag (
                                  id              bigint AUTO_INCREMENT,
                                  group_review_id bigint NOT NULL,
                                  tag             varchar(255) NOT NULL,
                                  CONSTRAINT FK_group_review_tag_group_review FOREIGN KEY (group_review_id) REFERENCES group_review (id),
                                  CONSTRAINT uk_group_review_tag_review_tag UNIQUE (group_review_id, tag),
                                  created_at      datetime(6) DEFAULT NULL,
                                  modified_at     datetime(6) DEFAULT NULL,
                                  PRIMARY KEY (id)
);

CREATE TABLE reading_progress_history (
    id          bigint AUTO_INCREMENT,
    activity_id bigint NOT NULL,
    user_id     bigint NOT NULL,
    percentage  bigint NOT NULL,
    created_at  datetime(6) DEFAULT NULL,
    modified_at datetime(6) DEFAULT NULL,

    CONSTRAINT FK_reading_progress_history_activity_id FOREIGN KEY (activity_id) REFERENCES activity (id),
    CONSTRAINT FK_reading_progress_history_user_id     FOREIGN KEY (user_id) REFERENCES user_profile (id),
    PRIMARY KEY (id)
);

CREATE TABLE highlight (
    id               bigint AUTO_INCREMENT,
    book_id          bigint NULL,
    author_id        bigint NULL,
    activity_id      bigint NULL,
    cfi              varchar(255) NOT NULL,
    spine            varchar(255) NOT NULL,
    memo             varchar(1000) NULL,
    highlightcontent varchar(1000) NULL,
    is_public        bit DEFAULT b'0' NOT NULL,
    created_at       datetime(6) DEFAULT NULL,
    modified_at      datetime(6) DEFAULT NULL,

    CONSTRAINT FK_highlight_book_id     FOREIGN KEY (book_id) REFERENCES ebook (id),
    CONSTRAINT FK_highlight_author_id   FOREIGN KEY (author_id) REFERENCES user_profile (id),
    CONSTRAINT FK_highlight_activity_id FOREIGN KEY (activity_id) REFERENCES activity (id),
    PRIMARY KEY (id)
);

CREATE TABLE highlight_comment (
    id           bigint AUTO_INCREMENT,
    highlight_id bigint NULL,
    author_id    bigint NULL,
    parent_id    bigint NULL,
    content      varchar(1000) NULL,
    created_at   datetime(6) DEFAULT NULL,
    modified_at  datetime(6) DEFAULT NULL,

    CONSTRAINT FK_highlight_comment_highlight_id FOREIGN KEY (highlight_id) REFERENCES highlight (id),
    CONSTRAINT FK_highlight_comment_author_id    FOREIGN KEY (author_id) REFERENCES user_profile (id),
    CONSTRAINT FK_highlight_comment_parent_id    FOREIGN KEY (parent_id) REFERENCES highlight_comment (id),
    PRIMARY KEY (id)
);

CREATE TABLE highlight_reaction (
    id            bigint AUTO_INCREMENT,
    highlight_id  bigint NULL,
    author_id     bigint NULL,
    comment_id    bigint NULL,
    reaction_type varchar(255) NOT NULL,
    created_at    datetime(6) DEFAULT NULL,
    modified_at   datetime(6) DEFAULT NULL,

    CONSTRAINT FK_highlight_reaction_highlight_id FOREIGN KEY (highlight_id) REFERENCES highlight (id),
    CONSTRAINT FK_highlight_reaction_author_id    FOREIGN KEY (author_id) REFERENCES user_profile (id),
    CONSTRAINT FK_highlight_reaction_comment_id   FOREIGN KEY (comment_id) REFERENCES highlight_comment (id),
    PRIMARY KEY (id)
);

CREATE TABLE discussion (
    id           bigint AUTO_INCREMENT,
    activity_id  bigint NULL,
    author_id    bigint NULL,
    title        varchar(255) NOT NULL,
    content      varchar(5000) NULL,
    is_debate    bit DEFAULT b'0' NOT NULL,
    created_at   datetime(6) DEFAULT NULL,
    modified_at  datetime(6) DEFAULT NULL,

    CONSTRAINT FK_discussion_activity_id FOREIGN KEY (activity_id) REFERENCES activity (id),
    CONSTRAINT FK_discussion_author_id   FOREIGN KEY (author_id) REFERENCES user_profile (id),
    PRIMARY KEY (id)
);
CREATE TABLE discussion_comment (
    id            bigint AUTO_INCREMENT,
    discussion_id bigint NULL,
    author_id     bigint NULL,
    parent_id     bigint NULL,
    content       varchar(1000) NULL,
    stance        varchar(255) NOT NULL,
    is_deleted    bit DEFAULT b'0' NOT NULL,
    is_edited     bit DEFAULT b'0' NOT NULL,
    created_at    datetime(6) DEFAULT NULL,
    modified_at   datetime(6) DEFAULT NULL,

    CONSTRAINT FK_discussion_comment_discussion_id FOREIGN KEY (discussion_id) REFERENCES discussion (id),
    CONSTRAINT FK_discussion_comment_author_id     FOREIGN KEY (author_id) REFERENCES user_profile (id),
    CONSTRAINT FK_discussion_comment_parent_id     FOREIGN KEY (parent_id) REFERENCES discussion_comment (id),
    PRIMARY KEY (id)
);

CREATE TABLE discussion_highlight (
    id            bigint AUTO_INCREMENT,
    discussion_id bigint NOT NULL,
    highlight_id  bigint NOT NULL,
    created_at    datetime(6) DEFAULT NULL,
    modified_at   datetime(6) DEFAULT NULL,

    CONSTRAINT FK_discussion_highlight_discussion_id FOREIGN KEY (discussion_id) REFERENCES discussion (id),
    CONSTRAINT FK_discussion_highlight_highlight_id  FOREIGN KEY (highlight_id) REFERENCES highlight (id),
    PRIMARY KEY (id)
);

CREATE TABLE notification (
    id                    bigint AUTO_INCREMENT,
    receiver_id           bigint NULL,
    sender_id             bigint NULL,
    group_id              bigint NULL,
    discussion_id         bigint NULL,
    discussion_comment_id bigint NULL,
    highlight_id          bigint NULL,
    message               varchar(255) NOT NULL,
    type                  varchar(255) NOT NULL,
    is_read               bit DEFAULT b'0' NOT NULL,
    created_at            datetime(6) DEFAULT NULL,
    modified_at           datetime(6) DEFAULT NULL,

    CONSTRAINT FK_notification_receiver_id           FOREIGN KEY (receiver_id) REFERENCES user_profile (id) ON DELETE SET NULL,
    CONSTRAINT FK_notification_sender_id             FOREIGN KEY (sender_id) REFERENCES user_profile (id) ON DELETE SET NULL,
    CONSTRAINT FK_notification_group_id              FOREIGN KEY (group_id) REFERENCES reading_group (id) ON DELETE SET NULL,
    CONSTRAINT FK_notification_discussion_id         FOREIGN KEY (discussion_id) REFERENCES discussion (id) ON DELETE SET NULL,
    CONSTRAINT FK_notification_discussion_comment_id FOREIGN KEY (discussion_comment_id) REFERENCES discussion_comment (id) ON DELETE SET NULL,
    CONSTRAINT FK_notification_highlight_id          FOREIGN KEY (highlight_id) REFERENCES highlight (id) ON DELETE SET NULL,
    PRIMARY KEY (id)
);