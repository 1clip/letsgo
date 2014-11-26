namespace java.swift coffee.letsgo.avatar

struct AvatarInfo {
  1: i64 avatar_id,
  2: i64 owner_id
}

exception AvatarNotFoundException {
  1: string msg
}

exception NotSupportedFormatException {
  1: string msg
}

service Avatar {
  AvatarInfo put(1: required i64 user_id, 2: required binary img) throws( 1: NotSupportedFormatException nsfe )
  binary get(1: required AvatarInfo info) throws( 1: AvatarNotFoundException anfe )
}