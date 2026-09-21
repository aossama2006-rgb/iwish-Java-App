package iwish.common;

/** Every request type a client can send to the server. */
public enum Action {
    REGISTER,
    LOGIN,
    LOGOUT,
    ADD_FRIEND,             // send a friend request (param: email)
    REMOVE_FRIEND,          // param: friendId
    GET_FRIENDS,
    GET_INCOMING_REQUESTS,
    ACCEPT_REQUEST,         // param: requestId
    DECLINE_REQUEST,        // param: requestId
    GET_CATALOG,            // all items users can pick from
    GET_MY_WISHLIST,
    ADD_TO_WISHLIST,        // params: itemId, note (optional)
    UPDATE_WISHLIST_ITEM,   // params: wishId, note
    REMOVE_FROM_WISHLIST,   // param: wishId
    GET_FRIEND_WISHLIST,    // param: friendId (must be a friend)
    CONTRIBUTE,             // params: wishId, amount (text, e.g. "250" or "99.50")
    GET_NOTIFICATIONS,
    MARK_NOTIFICATIONS_READ
}
