package es.unizar.urlshortener.core

class InvalidUrlException(val url: String) : Exception("[$url] does not follow a supported schema")

class NotReachableUrlException(val url: String) : Exception("[$url] is not reachable")

class RedirectionNotFound(val key: String) : Exception("[$key] is not known")
