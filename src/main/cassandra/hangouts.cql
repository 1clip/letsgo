CREATE TABLE letsgo.hangouts (
    hangout_id bigint primary key,
    creator_id bigint,
    activity varchar,
    subject varchar,
    location varchar,
    start_time timestamp,
    end_time timestamp,
    create_time timestamp,
    update_time timestamp,
    participators set <bigint>,
    cancelled boolean
);