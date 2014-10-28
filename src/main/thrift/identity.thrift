namespace java.swift coffee.letsgo.identity

include "avatar.thrift"

enum Gender {
  MALE = 0,
  FEMALE = 1
}

enum SignupType {
  CELL_PHONE = 0,
  FACEBOOK = 1
}

struct User {
  1: string login_name,
  2: optional string friendly_name,
  3: Gender gender,
  4: string date_of_birth,
  5: optional avatar.AvatarInfo avatar_info,
  6: optional string cell_phone,
  7: SignupType sign_up_type,
  8: optional string sign_up_token,
  9: string locale,
  10: optional i64 id
}

typedef map<i64, User> Users

exception UserNotFoundException {
  1: string message
}

service IdentityService {
  User create_user(1:User user)
  User get_user(1:i64 user_id)
  Users get_users(1:set<i64> ids)
}