namespace java.swift coffee.letsgo.iceflake

exception InvalidSystemClock {
  1: string message,
}

exception InvalidIdTypeError {
  1: string message,
}

service Iceflake {
  i64 get_worker_id()
  i64 get_timestamp()
  i64 get_id(1:i64 type)
}