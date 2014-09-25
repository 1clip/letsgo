namespace java.swift coffee.letsgo.identity

struct User {
  1: string name,
  2: string nick_name,
  3: byte gender,
  4: string dob,
  5: i64 avatarId,
  6: string phone,
  7: byte registrationSource,
  8: optional string registrationToken
  9: optional i64 id
}

service IdentityService {
  i64 postUser(1:User user)
  User getUser(1:i64 userId)
}