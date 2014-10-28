create table hangout_folks (
    user_id bigint,
    hangout_id bigint,
    role varchar,
    state varchar,
    comment varchar,
    update_time timestamp,
    primary key (user_id, hangout_id)
);