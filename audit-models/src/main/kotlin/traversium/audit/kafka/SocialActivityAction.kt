package traversium.audit.kafka

/**
 * Enum representing all possible social activity actions that can be audited
 * These actions are related to likes and comments on media within trips
 * @author Ozbej Pavc
 */
enum class SocialActivityAction {
    // Like actions
    LIKE_CREATED,
    LIKE_DELETED,
    
    // Comment actions
    COMMENT_CREATED,
    COMMENT_UPDATED,
    COMMENT_DELETED,
    COMMENT_REPLY
}

