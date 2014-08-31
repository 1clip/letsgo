namespace java.swift coffee.letsgo.iceflake

exception InvalidSystemClock {
  1: string message,
}

exception InvalidUserAgentError {
  1: string message,
}

service Iceflake {
  i64 get_datacenter_id()
  i64 get_worker_id()
  i64 get_timestamp()
  i64 get_id(1:string useragent)
}

struct AuditLogEntry {
  1: i64 id,
  2: string useragent,
  3: i64 tag
}