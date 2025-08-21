set names utf8mb4;
create table users
(
    id bigint primary key not null auto_increment,
    email varchar(255) null unique,
    password_hash varchar(255) not null,
    role enum('admin', 'maintainer', 'reader') not null,
    created_at timestamp default current_timestamp
)engine= innoDB default charset=utf8mb4 collate=utf8mb4_0900_ai_ci;
create index idx_users_role on users(role);
create table repositories(
                             id bigint primary key not null auto_increment,
                             owner_id bigint not null,
                             name varchar(255) not null,
                             visibility varchar(16) not null,
                             created_at timestamp default current_timestamp,
                             constraint fk_repos_owner
                                 foreign key (owner_id) references users(id)
                                     on delete cascade
) engine= innoDB default charset=utf8mb4 collate=utf8mb4_0900_ai_ci;
create index idx_repo_owner on repositories(owner_id);
create unique index uk_repo_owner_name on repositories(owner_id, name);
create table folders(
                        id bigint primary key not null auto_increment,
                        repo_id bigint not null,
                        parent_id bigint null,
                        name varchar(255) not null,
                        constraint fk_folders_repo
                            foreign key (repo_id) references repositories(id)
                                on delete cascade,
                        constraint fke_folders_parent
                            foreign key (parent_id) references folders(id)
                                on delete cascade
)engine= innoDB default charset=utf8mb4 collate=utf8mb4_0900_ai_ci;
create table files(
                      id bigint primary key not null auto_increment,
                      folder_id bigint not null,
                      latest_version_id bigint null,
                      name varchar(255) not null,
                      content_type varchar(128),
                      size bigint default 0,

                      constraint fk_files_folder
                          foreign key (folder_id) references folders(id)
                              on delete cascade
) engine= innoDB default charset=utf8mb4 collate=utf8mb4_0900_ai_ci;
create index idx_files_folder on files(folder_id);
create index idx_files_name on files(name);

create table file_versions(
                              id bigint primary key auto_increment,
                              file_id bigint not null,
                              version_no int not null,
                              storage_path varchar(512) not null,
                              sha256 char(64) not null,
                              size bigint not null default 0,
                              created_by bigint not null,
                              created_at timestamp not null default current_timestamp,

                              constraint fk_fv_file
                                  foreign key(file_id) references files(id)
                                      on delete cascade,

                              constraint fk_fv_creator
                                  foreign key(created_by) references users(id)
                                      on delete cascade,

                              constraint uk_fv_file_ver unique (file_id, version_no)
) engine= innoDB default charset=utf8mb4 collate=utf8mb4_0900_ai_ci;

create index idx_fv_file on file_versions(file_id);
create index idx_fv_creator on file_versions(created_by);
create index idx_fv_createdat on file_versions(created_at);

alter table files
    add constraint fk_files_latest_version
        foreign key (latest_version_id) references file_versions(id)
            on delete set null;

create table permissions(
                            id bigint primary key auto_increment,
                            repo_id bigint not null,
                            user_id bigint not null,
                            role enum('admin', 'maintainer', 'reader') not null,

                            constraint fk_perm_repo
                                foreign key(repo_id) references repositories(id)
                                    on delete cascade,

                            constraint fk_perm_user
                                foreign key(user_id) references users(id)
                                    on delete cascade,

                            constraint uk_perm_repo_user unique (repo_id, user_id)
) engine= innoDB default charset=utf8mb4 collate=utf8mb4_0900_ai_ci;

create index idx_perm_user_repo on permissions(user_id, repo_id);

create table audit_logs(
                           id bigint primary key auto_increment,
                           actor_id bigint null,
                           action varchar(32) not null,
                           target_type varchar(32) not null,
                           target_id bigint not null,
                           at timestamp not null default current_timestamp,
                           ip varchar(64),
                           user_agent varchar(255),

                           constraint fk_audit_actor
                               foreign key (actor_id) references users(id)
                                   on delete set null
)engine= innoDB default charset=utf8mb4 collate=utf8mb4_0900_ai_ci;

create index idx_audit_actor_at on audit_logs(actor_id, at);
create index idx_audit_target_at on audit_logs(target_type, target_id, at);

create table refresh_tokens(
                               id bigint primary key auto_increment,
                               user_id bigint not null,
                               token varchar(512) not null unique,
                               expires_at timestamp not null,
                               revoked tinyint not null default 0,

                               constraint fk_rt_user
                                   foreign key(user_id) references users(id)
                                       on delete cascade
)engine= innoDB default charset=utf8mb4 collate=utf8mb4_0900_ai_ci;
create index idx_rt_user_state on refresh_tokens(user_id, revoked, expires_at);