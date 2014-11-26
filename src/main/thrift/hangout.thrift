namespace java.swift coffee.letsgo.hangout

include "avatar.thrift"

enum Role {
    ORGANIZER = 0,
    INVITEE = 1
}

enum HangoutState {
    ACTIVE = 0,
    HAPPENING = 1,
    OVERDUE = 2,
    CANCELED = 3
}

enum ParticipatorState {
    PENDING = 0,
    ACCEPTED = 1,
    REJECTED = 2
}

struct Participator {
   1: i64 id
   2: optional ParticipatorState state
   3: optional string comment
   4: Role role
   5: optional string login_name
   6: optional string friendly_name
   7: optional avatar.AvatarInfo avatar_info
   8: optional string error_code
}

struct HangoutSummary {
   1: i64 id
   2: string activity
   3: string subject
   4: Participator organizer
   5: HangoutState state
   6: i32 num_accepted
   7: i32 num_rejected
   8: i32 num_pending
}

struct Hangout {
   1: optional i64 id
   2: string activity
   3: string subject
   4: string location
   5: string start_time
   6: string end_time
   7: HangoutState state
   8: list<Participator> participators
}

struct HangoutInfo {
   1: string activity
   2: string subject
   3: string location
   4: string start_time
   5: string end_time
   6: HangoutState state
   7: list<i64> participators
   8: i32 num_accepted
   9: i32 num_rejected
   10:i32 num_pending
   11: i64 organizer_id
}

struct UserHangoutInfo {
   1: i64 hangout_id
   2: ParticipatorState state
   3: optional string comment
   4: Role role
   5: i64 user_id
}

exception InvalidHangoutDataException {
  1: string msg
}

exception HangoutProcessException {
  1: string msg
}

exception HangoutNotFoundException {
  1: string msg
}

service HangoutService {
    Hangout createHangout(1: required i64 userId, 2: required Hangout hangOut)
        throws (
            1: InvalidHangoutDataException ihde,
            2: HangoutProcessException hpe
        )
    Hangout getHangoutById(1: required i64 userId, 2: required i64 hangOutId)
        throws (
            1: HangoutNotFoundException hnfe,
            2: HangoutProcessException hpe
        )
    list<HangoutSummary> getHangoutByStatus(1:required i64 userId, 2: required HangoutState status)
        throws (
            1: HangoutProcessException hpe
        )
    void updateHangout(1: required i64 userId, 2: required i64 hangOutId, 3: required Hangout hangout)
        throws (
            1: InvalidHangoutDataException ihde,
            2: HangoutNotFoundException hnfe,
            3: HangoutProcessException hpe
        )
}