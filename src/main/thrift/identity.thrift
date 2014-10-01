namespace java.swift coffee.letsgo.identity

struct AvatarInfo {
  1: i64 avatar_id,
  2: i64 avatar_token
}

enum Gender {
  MALE = 0,
  FEMALE = 1
}

enum SignupType {
  CELL_PHONE = 0,
  FACEBOOK = 1
}

struct NewUser {
  1: string login_name,
  2: string friendly_name,
  3: Gender gender,
  4: string date_of_birth,
  5: optional AvatarInfo avatar
  6: string cell_phone,
  7: SignupType sign_up_type,
  8: string sign_up_token,
}

struct UserInfo {
  1: i64 user_id,
  2: string login_name,
  3: string friendly_name,
  4: Gender gender,
  5: string date_of_birth,
  6: optional AvatarInfo avatar,
  7: string cell_phone
}

service IdentityService {
  UserInfo create_user(1:NewUser user)
  UserInfo get_user(1:i64 user_id)
}