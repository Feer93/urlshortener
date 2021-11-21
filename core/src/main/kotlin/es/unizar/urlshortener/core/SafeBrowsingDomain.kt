package es.unizar.urlshortener.core

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

enum class ThreatType {
    MALWARE,
    SOCIAL_ENGINEERING,
    UNWANTED_SOFTWARE,
    THREAT_TYPE_UNSPECIFIED,
    POTENTIALLY_HARMFUL_APPLICATION
}

enum class PlatformType {
    LINUX,
    WINDOWS,
    ANDROID,
    OSX,
    IOS,
    CHROME,
    PLATFORM_TYPE_UNSPECIFIED,
    ANY_PLATFORM,
    ALL_PLATFORMS
}

enum class ThreatEntryRequestType {
    URL,
    HASH,
    DIGEST
}

enum class ThreatEntryType {
    URL,
    EXECUTABLE,
    THREAT_ENTRY_TYPE_UNSPECIFIED
}


@JsonInclude(JsonInclude.Include.NON_NULL)
class ThreatEntry(value: String, type: ThreatEntryRequestType) {
    @JsonProperty("hash")
    var hash: String? = null
    @JsonProperty("url")
    var url: String? = null
    @JsonProperty("digest")
    var digest: String? = null

    init {
        setOneValue(value, type)
    }

    private fun setOneValue(value: String, type: ThreatEntryRequestType) {
        if (type == ThreatEntryRequestType.URL) {
            url = value
        } else if (type == ThreatEntryRequestType.DIGEST) {
            digest = value
        } else if (type == ThreatEntryRequestType.HASH) {
            hash = value
        }
        arrayOf<ThreatEntryRequestType>(ThreatEntryRequestType.HASH, ThreatEntryRequestType.HASH)
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class ThreatInfo(
        @JsonProperty("threatTypes")
        var threatTypes: List<ThreatType>? = null,
        @JsonProperty("platformTypes")
        var platformTypes: List<PlatformType>? = null,
        @JsonProperty("threatEntryTypes")
        var threatEntryTypes: List<ThreatEntryType>? = null,
        @JsonProperty("threatEntries")
        var threatEntries: List<ThreatEntry>? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class ThreatMatchesRequest(
    @JsonProperty("threatInfo") 
    var threatInfo: ThreatInfo? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
class ThreatMatch() {
    @JsonProperty("threatType")
    var threatType: ThreatType? = null
    @JsonProperty("platformType")
    var platformType: PlatformType? = null
    @JsonProperty("threatEntryType")
    var threatEntryType: ThreatEntryType? = null
    @JsonProperty("cacheDuration")
    var cacheDuration: String? = null
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class ThreatMatchesResponse() {
    @JsonProperty("matches")
    var matches: List<ThreatMatch>? = null
}
