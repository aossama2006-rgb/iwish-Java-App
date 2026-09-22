package iwish.common;

public enum Action {

    //  Member 1 (Authentication & User Management): sign-in, registration, session 
    REGISTER,
    LOGIN,
    LOGOUT,

    //  Member 2 (Friends Management): add/remove friends, accept/decline requests 
    // send a friend request (param: email)
    ADD_FRIEND,         
    // param: friendId
    REMOVE_FRIEND,          
    GET_FRIENDS,
    GET_INCOMING_REQUESTS,
    // param: requestId
    ACCEPT_REQUEST,         
    // param: requestId
    DECLINE_REQUEST,        

    // Member 3 (Wishlist & Catalog): browsing the catalog, building a wish list
    GET_CATALOG,            
    // all items users can pick from
    GET_MY_WISHLIST,
    ADD_TO_WISHLIST,        
    // params: itemId, note 
    UPDATE_WISHLIST_ITEM,   
    // params: wishId, note
    REMOVE_FROM_WISHLIST,   
    // param: wishId
    GET_FRIEND_WISHLIST,    
    // param: friendId (must be a friend)

    // Member 4 (Contributions & Notifications): paying towards a gift, being notified
    CONTRIBUTE,             
    // params: wishId, amount (text, e.g. "250" or "99.50")
    GET_NOTIFICATIONS,
    MARK_NOTIFICATIONS_READ
}
