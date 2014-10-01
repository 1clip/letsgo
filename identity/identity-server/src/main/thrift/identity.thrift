namespace java.swift coffee.letsgo.identity

struct User {
  1: string name,
  2: string friendly_name,
  3: byte gender,
  4: string dob,
  5: optional i64 avatar_id,
  6: optional string avatar_token,
  7: string cell_phone,
  8: byte registration_source,
  9: optional string registration_token,
  10: i64 id
}

service IdentityService {
  i64 postUser(1:User user)
  User getUser(1:i64 userId)
}