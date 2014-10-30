namespace java.swift coffee.letsgo.graph

include "avatar.thrift"

enum InvitationState {
    PENDING = 0,
    ACCEPT = 1,
    REJECT = 2
}

struct Group {
  1: i64 id
  2: string name
}

struct Friend {
  1: i64 id
  2: string friendly_name
  3: avatar.AvatarInfo avatarInfo
  4: list<Group> groups
}

struct Invitation {
  1: string message
  2: list<i64> receivers
}

struct InvitationInfo {
  1: i64 id
  2: i64 sender_id
  3: string sender_friendly_name
  4: i64 receiver_id
  5: string receiver_friendly_name
  6: string message
  7: InvitationState state
}

service GraphService {
   list<Group> getGroups(1:i64 userId)
   list<Friend> getFriends(1:i64 userId)
   list<InvitationInfo> createInvitation(Invitation invitation)
   void updateInvitationState(1:i64 userId, 2:i64 invitationId, 3:InvitationState state)
}