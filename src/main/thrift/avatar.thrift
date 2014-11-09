namespace java.swift coffee.letsgo.avatar

struct AvatarInfo {
  1: i64 avatar_id,
  2: i64 owner_id
}

service Avatar {
  AvatarInfo put(1: required binary img)
  binary get(1: required AvatarInfo info)
}