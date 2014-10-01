namespace java.swift coffee.letsgo.iceflake

exception InvalidSystemClock {
  1: string message,
}

exception InvalidIdTypeError {
  1: string message,
}

enum IdType {
  ACCT_ID = 0,
  TAG_ID = 1,
  HANGOUT_ID = 2;
}

service Iceflake {
  i64 get_worker_id()
  i64 get_timestamp()
  i64 get_id(1:IdType type)
}