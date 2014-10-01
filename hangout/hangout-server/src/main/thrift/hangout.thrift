namespace java.swift coffee.letsgo.identity

struct Invitee {
   1: i64 id
   2: optional string state
   3: optional string comment
}

struct HangoutSummary {
   1: i64 id
   2: string type
   3: string status
   4: i32 receivedNumber
   5: i32 decliendNumber
   6: i32 pendingNumber
}

struct Hangout {
   1: optional i64 id
   2: string type
   3: string description
   4: string location
   5: i64 startTime
   6: i64 endTime
   7: list<Invitee> invitees
}

service HangoutService {
    i64 postHangout(1:Hangout hangOut)
    Hangout getHangoutById(1:i64 hangOutId)
    Hangout getHangoutByIdAndStatus(1:i64 hangOutId, 2:string status)
}