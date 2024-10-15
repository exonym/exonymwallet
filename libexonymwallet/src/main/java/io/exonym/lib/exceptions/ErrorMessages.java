package io.exonym.lib.exceptions;

public class ErrorMessages {


    /**
     * Generic error, when insufficient parameters have been provided
     * Check required[]
     */
    public static final String INCORRECT_PARAMETERS = "ERROR_INCORRECT_PARAMETERS";
    public static final String SSO_END_POINT_404 = "SSO_END_POINT_404";
    public static final String MODERATOR_DOES_NOT_ACCEPT_OPEN_JOIN_REQUESTS = "MODERATOR_DOES_NOT_ACCEPT_OPEN_JOIN_REQUESTS";
    public static final String TIME_OUT = "TIME_OUT";

    public static final String BLACKLISTED_MODERATOR = "BLACKLISTED_MODERATOR";
    public static final String BLACKLISTED_LEAD = "BLACKLISTED_LEAD";

    /**
     * Generic error, when we reset a cors condition
     * Check required[]
     */
    public static final String CORS_CUSTOM = "CORS_CUSTOM";

    /**
     * This will return the appUuid of the incompletely registered device in the info array
     */
    public static final String REGISTER_ONE_DEVICE_AT_A_TIME = "REGISTER_ONE_DEVICE_AT_A_TIME";

    /**
     * This will return the appUuid of the incompletely registered device in the info array
     */
    public static final String DB_TAMPERING = "DB_TAMPERING";

    /**
     * This will return the appUuid of the incompletely registered device in the info array
     */
    public static final String LENGTH_OF_MESSAGE_ERROR = "LENGTH_OF_MESSAGE_ERROR";

    /**
     *
     */
    public static final String REVOKED = "REVOKED";

    /**
     *
     */
    public static final String SESSION_EXPIRED = "SESSION_EXPIRED";

    /**
     *
     */
    public static final String QR_EXPIRED = "QR_EXPIRED";

    /**
     *
     */
    public static final String DUPLICATE_ACTION = "DUPLICATE_ACTION";

    /**
     *
     */
    public static final String ALREADY_APPROVED = "ALREADY_APPROVED";

    /**
     * Generic server error.
     */
    public static final String SERVER_SIDE_PROGRAMMING_ERROR = "SERVER_SIDE_PROGRAMMING_ERROR";

    /**
     * A public key was not in the expected format.
     */
    public static final String INVALID_KEY_SPEC = "INVALID_KEY_SPEC";


    /**
     * Unable to discover the requested context.
     */
    public static final String UNEXPECTED_TOKEN_FOR_THIS_NODE_OR_AUTH_TIMEOUT = "UNEXPECTED_TOKEN_FOR_THIS_NODE_OR_AUTH_TIMEOUT";

    /**
     *
     */
    public static final String MODERATOR_NOT_FOUND_ON_NETWORK_MAP = "MODERATOR_NOT_FOUND_ON_NETWORK_MAP";
    /**
     *
     */
    public static final String NETWORK_MAP_REFRESH_FAILURE = "NETWORK_MAP_REFRESH_FAILURE";

    /**
     *
     */
    public static final String HOST_NOT_INITIALIZED = "HOST_NOT_INITIALIZED";

    /**
     *
     */
    public static final String INVALID_EMAIL = "INVALID_EMAIL";

    /**
     *
     */
    public static final String BANNED_UNTIL = "BANNED_UNTIL";

    /**
     * Timed out
     */
    public static final String STALE_REQUEST = "STALE_REQUEST";

    /**
     * Failed to find an x-node-description.xml file
     */
    public static final String NO_DESCRIPTION_FOUND = "NO_DESCRIPTION_FOUND";


    /**
     *
     */
    public static final String INVALID_APP_UUID = "INVALID_APP_UUID";

    /**
     *
     */
    public static final String APP_UUID_ALREADY_REGISTERED = "APP_UUID_ALREADY_REGISTERED";

    /**
     *
     */
    public static final String ALREADY_SUBSCRIBED = "ALREADY_SUBSCRIBED";

    /**
     *
     */
    public static final String USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS";
    public static final String USER_DOES_NOT_EXIST = "USER_DOES_NOT_EXIST";

    /**
     *
     */
    public static final String WORDLIST_ALREADY_EXISTS = "WORDLIST_ALREADY_EXISTS";


    /**
     *
     */
    public static final String DATABASE_ACCESS_ERROR = "DATABASE_ACCESS_ERROR";

    /**
     *
     */
    public static final String FAILED_TO_AUTHORIZE = "FAILED_TO_AUTHORIZE";

    /**
     *
     */
    public static final String PROFILE_REQUIRED = "PROFILE_REQUIRED";

    /**
     *
     */
    public static final String SIGN_IN_AGAIN_TOKEN_STALE = "SIGN_IN_AGAIN_TOKEN_STALE";

    /**
     * Unless you maintain the session you must also pass in {kid, key}
     */
    public static final String NO_SESSION_MAINTAINED = "NO_SESSION_MAINTAINED";

    /**
     *
     */
    public static final String INVALID_UID = "INVALID_UID";

    /**
     *
     */
    public static final String API_KEY_NOT_FOUND = "API_KEY_NOT_FOUND";

    /**
     *
     */
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";

    /**
     *
     */
    public static final String PROFILE_NOT_FOUND = "PROFILE_NOT_FOUND";

    /**
     *
     */
    public static final String INSUFFICIENT_PRIVILEGES = "INSUFFICIENT_PRIVILEGES";

    /**
     *
     */
    public static final String EXPECTED_COMMAND = "EXPECTED_COMMAND";

    /**
     * There is a short window that the user can reset their password
     * after the password has been set, and before the challenge is complete.
     */
    public static final String USE_PASSWORD_RESET = "USE_PASSWORD_RESET";

    /**
     *
     */
    public static final String UNKNOWN_COMMAND = "UNKNOWN_COMMAND";

    /**
     *
     */
    public static final String B64_ENCODING_ERROR = "B64_ENCODING_ERROR";

    /**
     *
     */
    public static final String INVALID_PASSWORD = "INVALID_PASSWORD";

    public static final String HEXADECIMAL_REQUIRED = "HEXADECIMAL_REQUIRED";
    public static final String PASSWORD_DID_NOT_MATCH = "PASSWORD_DID_NOT_MATCH";

    public static final String PASSWORD_L7_UPPER_LOWER_ONE_NUMBER = "PASSWORD_L7_UPPER_LOWER_ONE_NUMBER";

    /**
     * Not a hash
     */
    public static final String INVALID_PASSWORD_SPEC = "INVALID_PASSWORD_SPEC";

    /**
     * The Passport checksums did not validate correctly, occurs either
     * after a bad read or with tampering
     */
    public static final String DOCUMENT_CHECK_ERROR = "DOCUMENT_CHECK_ERROR";

    /**
     * The server attempts to massage the data to be valid.
     * this message is displayed if it fails.
     */
    public static final String UNABLE_TO_READ_PASSPORT_DATA = "UNABLE_TO_READ_PASSPORT_DATA";

    /**
     * The passport cannot be expiring within 6 months.
     */
    public static final String PASSPORT_NEAR_TO_EXPIRY = "PASSPORT_NEAR_TO_EXPIRY";

    /**
     * We believe the user is trying to clone: warn of consequences.
     */
    public static final String SYBIL_WARN = "SYBIL_WARN";

    /**
     * 13 < age < 70
     */
    public static final String AGE_REQUIRES_MANUAL_REGISTRATION = "AGE_REQUIRES_MANUAL_REGISTRATION";

    /**
     *
     */
    public static final String IMAGE_ENCODING_ERROR = "IMAGE_ENCODING_ERROR";

    /**
     *
     */
    public static final String NO_RESULTS_FOR_CRITERIA = "NO_RESULTS_FOR_CRITERIA";

    /**
     *
     */
    public static final String REGISTRATION_DELAY = "REGISTRATION_DELAY";

    /**
     *  The user will receive a call reference because they haven't been banned
     *
     *  We need to structure a suitable message and guide the user to a successful registration
     */
    public static final String REGISTRATION_REJECTION = "REGISTRATION_REJECTION";

    /**
     *
     */
    public static final String UNINSTALL = "UNINSTALL";

    /**
     *
     */
    public static final String DEVICE_BLOCKED = "DEVICE_BLOCKED";

    /**
     *
     */
    public static final String REGISTRATION_IN_PROGRESS = "REGISTRATION_IN_PROGRESS";

    /**
     *
     */
    public static final String REGISTRATION_HAS_BEEN_DELAYED = "REGISTRATION_HAS_BEEN_DELAYED";

    /**
     *
     */
    public static final String REGISTRATION_REJECTED_TRY_AGAIN = "REGISTRATION_REJECTED_TRY_AGAIN";

    /**
     *
     */
    public static final String CANNOT_DELETE_DEVICE = "CANNOT_DELETE_DEVICE";

    /**
     *
     */
    public static final String NO_EXONYMS_IN_TOKEN = "NO_EXONYMS_IN_TOKEN";

    /**
     *
     */
    public static final String PROOF_IS_OUT_OF_SCOPE = "PROOF_IS_OUT_OF_SCOPE";

    public static final String OUT_OF_RANGE = "OUT_OF_RANGE";

    /**
     *
     */
    public static final String TOKEN_INVALID = "TOKEN_INVALID";

    /**
     *
     */
    public static final String AGREE_TO_TERMS = "AGREE_TO_TERMS";


    /**
     *
     */
    public static final String EXPIRED_PSEUDONYM_REQUEST = "EXPIRED_PSEUDONYM_REQUEST";

    /**
     *
     */
    public static final String UNEXPECTED_PSEUDONYM_REQUEST = "UNEXPECTED_PSEUDONYM_REQUEST";
    public static final String INSPECTION_RESULT_REQUIRED = "INSPECTION_RESULT_REQUIRED";

    /**
     *
     */
    public static final String URL_NOT_FOUND = "URL_NOT_FOUND";

    /**
     *
     */
    public static final String URL_INVALID = "URL_INVALID";

    /**
     *
     */
    public static final String FILE_NOT_FOUND = "FILE_NOT_FOUND";

    public static final String WRITE_FILE_ERROR = "WRITE_FILE_ERROR";

    public static final String FILE_NOT_REGULAR = "FILE_NOT_REGULAR";

    /**
     *
     */
    public static final String INVALID_COMMENT = "INVALID_COMMENT";

    /**
     *
     */
    public static final String CANNOT_REMOVE_COMMENT_WITH_REPLIES = "CANNOT_REMOVE_COMMENT_WITH_REPLIES";    /**

     *
     *
     */
    public static final String SEND_GRID_VERIFY_SINGLE_SENDER_ADDRESS = "SEND_GRID_VERIFY_SINGLE_SENDER_ADDRESS";

    /*
     *
     */
    public static final String SEND_GRID_API_KEY_FAILURE = "SEND_GRID_API_KEY_FAILURE";

    /*
     *
     */
    public static final String MISSING_EXONYMS = "MISSING_EXONYMS";

    /**
     *
     */
    public static final String POKE_NOT_FOUND = "POKE_NOT_FOUND";

    /**
     *
     */
    public static final String SIGNATURES_NOT_FOUND = "SIGNATURES_NOT_FOUND";

    /**
     *
     */
    public static final String NIBBLE6_NOT_FOUND = "NIBBLE6_NOT_FOUND";

    /**
     *
     */
    public static final String PAGE_LIMIT_REACHED = "PAGE_LIMIT_REACHED";

    /**
     *
     */
    public static final String SCREEN_NAME_IN_USE = "SCREEN_NAME_IN_USE";

}
