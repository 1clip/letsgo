namespace java.swift coffee.letsgo.iceflake

enum IdType {
  ACCT_ID = 0,
  TAG_ID = 1,
  HANGOUT_ID = 2;
}

exception InvalidIdTypeException {
  1: string msg
}

exception InvalidSystemClockException {
  1: string msg
}

service Iceflake {
  i64 get_worker_id()
  i64 get_timestamp()
  i64 get_id(1: required IdType type)
      throws (
        1: InvalidIdTypeException iite,
        2: InvalidSystemClockException isce
      )
}