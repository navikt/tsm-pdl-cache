package no.nav.tsm.pdl.cache.core

import tools.jackson.databind.DeserializationFeature
import tools.jackson.module.kotlin.jacksonMapperBuilder

val exposedObjectMapper =
    jacksonMapperBuilder().enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT).build()
