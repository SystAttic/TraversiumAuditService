package traversium.audit.kafka

/**
 * Utility class for mapping between service-specific event types and audit activity actions
 * This provides a centralized way to convert events from UserService, TripService, and SocialService
 * into standardized audit activity actions
 * @author Ozbej Pavc
 */
object ActivityActionMapper {
    
    /**
     * Maps UserEvent from UserService to UserActivityAction
     * @param userEvent The event from UserService
     * @return The corresponding UserActivityAction
     */
    fun mapUserEvent(userEvent: String): UserActivityAction {
        return when (userEvent.uppercase()) {
            "USER_CREATED" -> UserActivityAction.USER_CREATED
            "USER_DELETED" -> UserActivityAction.USER_DELETED
            "USER_UPDATED" -> UserActivityAction.USER_ACCOUNT_DETAILS_CHANGED
            else -> throw IllegalArgumentException("Unknown user event: $userEvent")
        }
    }
    
    /**
     * Maps TripEventType from TripService to TripActivityAction
     * @param tripEventType The event type from TripService
     * @return The corresponding TripActivityAction
     */
    fun mapTripEventType(tripEventType: String): TripActivityAction {
        return when (tripEventType.uppercase()) {
            "TRIP_CREATED" -> TripActivityAction.TRIP_CREATED
            "TRIP_DELETED" -> TripActivityAction.TRIP_DELETED
            "TRIP_UPDATED" -> TripActivityAction.TRIP_INFO_CHANGED
            "COLLABORATOR_ADDED" -> TripActivityAction.TRIP_COLLABORATOR_INVITED
            "COLLABORATOR_DELETED" -> TripActivityAction.TRIP_COLLABORATOR_REMOVED
            "VIEWER_ADDED" -> TripActivityAction.TRIP_VIEWER_INVITED
            "VIEWER_DELETED" -> TripActivityAction.TRIP_VIEWER_REMOVED
            else -> throw IllegalArgumentException("Unknown trip event type: $tripEventType")
        }
    }
    
    /**
     * Maps AlbumEventType from TripService to TripActivityAction
     * @param albumEventType The event type from TripService
     * @return The corresponding TripActivityAction
     */
    fun mapAlbumEventType(albumEventType: String): TripActivityAction {
        return when (albumEventType.uppercase()) {
            "ALBUM_CREATED" -> TripActivityAction.ALBUM_CREATED
            "ALBUM_DELETED" -> TripActivityAction.ALBUM_DELETED
            "ALBUM_UPDATED" -> TripActivityAction.ALBUM_INFO_CHANGED
            else -> throw IllegalArgumentException("Unknown album event type: $albumEventType")
        }
    }
    
    /**
     * Maps MediaEventType from TripService to TripActivityAction
     * @param mediaEventType The event type from TripService
     * @return The corresponding TripActivityAction
     */
    fun mapMediaEventType(mediaEventType: String): TripActivityAction {
        return when (mediaEventType.uppercase()) {
            "MEDIA_ADDED" -> TripActivityAction.MEDIA_UPLOADED
            "MEDIA_DELETED" -> TripActivityAction.MEDIA_DELETED
            else -> throw IllegalArgumentException("Unknown media event type: $mediaEventType")
        }
    }
    
    /**
     * Maps a specific user account detail change to the appropriate UserActivityAction
     * @param changedField The name of the field that was changed
     * @return The corresponding UserActivityAction for the specific change
     */
    fun mapUserDetailChange(changedField: String): UserActivityAction {
        return when (changedField.lowercase()) {
            "email" -> UserActivityAction.USER_EMAIL_CHANGED
            "password" -> UserActivityAction.USER_PASSWORD_CHANGED
            "displayname", "display_name" -> UserActivityAction.USER_DISPLAY_NAME_CHANGED
            "username" -> UserActivityAction.USER_USERNAME_CHANGED
            "description" -> UserActivityAction.USER_DESCRIPTION_CHANGED
            "avatarphotoreference", "avatar_photo_reference" -> UserActivityAction.USER_AVATAR_PHOTO_CHANGED
            "coverphotoreference", "cover_photo_reference" -> UserActivityAction.USER_COVER_PHOTO_CHANGED
            "firstname", "first_name" -> UserActivityAction.USER_FIRST_NAME_CHANGED
            "lastname", "last_name" -> UserActivityAction.USER_LAST_NAME_CHANGED
            "countryoforigin", "country_of_origin" -> UserActivityAction.USER_COUNTRY_OF_ORIGIN_CHANGED
            "gender" -> UserActivityAction.USER_GENDER_CHANGED
            else -> UserActivityAction.USER_ACCOUNT_DETAILS_CHANGED
        }
    }
    
    /**
     * Maps a specific trip info change to the appropriate TripActivityAction
     * @param changedField The name of the field that was changed
     * @return The corresponding TripActivityAction for the specific change
     */
    fun mapTripInfoChange(changedField: String): TripActivityAction {
        return when (changedField.lowercase()) {
            "title", "name" -> TripActivityAction.TRIP_NAME_CHANGED
            "description" -> TripActivityAction.TRIP_DESCRIPTION_CHANGED
            "coverphotourl", "cover_photo_url" -> TripActivityAction.TRIP_COVER_PHOTO_CHANGED
            "visibility" -> TripActivityAction.TRIP_VISIBILITY_CHANGED
            else -> TripActivityAction.TRIP_INFO_CHANGED
        }
    }
    
    /**
     * Maps a specific album info change to the appropriate TripActivityAction
     * @param changedField The name of the field that was changed
     * @return The corresponding TripActivityAction for the specific change
     */
    fun mapAlbumInfoChange(changedField: String): TripActivityAction {
        return when (changedField.lowercase()) {
            "title" -> TripActivityAction.ALBUM_TITLE_CHANGED
            "description" -> TripActivityAction.ALBUM_DESCRIPTION_CHANGED
            else -> TripActivityAction.ALBUM_INFO_CHANGED
        }
    }
    
    /**
     * Maps social activity actions from SocialService to SocialActivityAction
     * @param action The action string from SocialService
     * @return The corresponding SocialActivityAction
     */
    fun mapSocialActivityAction(action: String): SocialActivityAction {
        return when (action.uppercase()) {
            "LIKE_CREATED" -> SocialActivityAction.LIKE_CREATED
            "LIKE_DELETED" -> SocialActivityAction.LIKE_DELETED
            "COMMENT_CREATED" -> SocialActivityAction.COMMENT_CREATED
            "COMMENT_UPDATED" -> SocialActivityAction.COMMENT_UPDATED
            "COMMENT_DELETED" -> SocialActivityAction.COMMENT_DELETED
            "COMMENT_REPLY" -> SocialActivityAction.COMMENT_REPLY
            else -> throw IllegalArgumentException("Unknown social activity action: $action")
        }
    }

    /**
     * Maps file storage activity actions from FileStorageService to FileStorageActivityAction
     * @param action The action string from FileStorageService
     * @return The corresponding FileStorageActivityAction
     */
    fun mapFileStorageActivityActivityAction(action: String): FileStorageActivityAction {
        return when (action.uppercase()) {
            "FILE_UPLOADED" -> FileStorageActivityAction.FILE_UPLOADED
            "FILE_DELETED" -> FileStorageActivityAction.FILE_DELETED
            else -> throw IllegalArgumentException("Unknown social activity action: $action")
        }
    }
}

