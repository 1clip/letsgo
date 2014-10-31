CREATE TABLE letsgo.users (
    user_id bigint primary key,
    avatar_id bigint,
    login_name varchar,
    friendly_name varchar,
    gender int,
    date_of_birth timestamp,
    cell_phone varchar,
    sign_up_type int,
    sign_up_token varchar,
    locale varchar,
    create_time timestamp,
    update_time timestamp
);