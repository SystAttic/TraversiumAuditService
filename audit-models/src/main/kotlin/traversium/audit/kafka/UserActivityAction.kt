package traversium.audit.kafka

/**
 * Enum representing all possible user activity actions that can be audited
 * These actions are related to user account management and social interactions
 * @author Ozbej Pavc
 */
enum class UserActivityAction {
    // User lifecycle actions
    USER_CREATED,
    USER_DELETED,
    USER_SUSPENDED,
    
    // User blocking actions
    USER_BLOCKED,
    USER_UNBLOCKED,
    
    // User following actions
    USER_FOLLOWED,
    USER_UNFOLLOWED,
    
    // User account detail changes
    USER_ACCOUNT_DETAILS_CHANGED,
    USER_EMAIL_CHANGED,
    USER_PASSWORD_CHANGED,
    USER_DISPLAY_NAME_CHANGED,
    USER_USERNAME_CHANGED,
    USER_DESCRIPTION_CHANGED,
    USER_AVATAR_PHOTO_CHANGED,
    USER_COVER_PHOTO_CHANGED,
    USER_FIRST_NAME_CHANGED,
    USER_LAST_NAME_CHANGED,
    USER_COUNTRY_OF_ORIGIN_CHANGED,
    USER_GENDER_CHANGED
}

