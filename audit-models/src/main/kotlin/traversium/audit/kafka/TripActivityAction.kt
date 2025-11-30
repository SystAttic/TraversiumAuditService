package traversium.audit.kafka

/**
 * Enum representing all possible trip activity actions that can be audited
 * These actions are related to trips, albums, media, and trip collaboration
 * @author Ozbej Pavc
 */
enum class TripActivityAction {
    // Trip lifecycle actions
    TRIP_CREATED,
    TRIP_DELETED,
    
    // Trip collaborator actions
    TRIP_COLLABORATOR_INVITED,
    TRIP_COLLABORATOR_JOINED,
    TRIP_COLLABORATOR_REMOVED,
    
    // Trip viewer actions
    TRIP_VIEWER_INVITED,
    TRIP_VIEWER_JOINED,
    TRIP_VIEWER_REMOVED,
    
    // Trip info changes
    TRIP_INFO_CHANGED,
    TRIP_NAME_CHANGED,
    TRIP_DESCRIPTION_CHANGED,
    TRIP_COVER_PHOTO_CHANGED,
    TRIP_VISIBILITY_CHANGED,
    
    // Album actions
    ALBUM_CREATED,
    ALBUM_DELETED,
    ALBUMS_REORDERED,
    ALBUM_INFO_CHANGED,
    ALBUM_TITLE_CHANGED,
    ALBUM_DESCRIPTION_CHANGED,
    
    // Media actions
    MEDIA_UPLOADED,
    MEDIA_DELETED,
    MEDIA_ASSIGNED_TO_ALBUM,
    MEDIA_UNASSIGNED_FROM_ALBUM
}

