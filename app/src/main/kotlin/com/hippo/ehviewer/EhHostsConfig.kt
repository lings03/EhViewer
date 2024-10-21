package com.hippo.ehviewer

import com.hippo.ehviewer.client.hosts
import com.hippo.ehviewer.client.hostsDsl

val dFEnabledDomains = listOf(
    "github.com", "api.github.com",
    "ehgt.org", "gt0.ehgt.org", "gt1.ehgt.org", "gt2.ehgt.org", "gt3.ehgt.org", "ul.ehgt.org",
    "e-hentai.org", "api.e-hentai.org", "forums.e-hentai.org", "repo.e-hentai.org", "upload.e-hentai.org",
    "exhentai.org", "s.exhentai.org",
)

val builtInHosts = hostsDsl {
    hosts("exhentai.org", "s.exhentai.org") {
        "178.175.128.251" blockedInCN false
        "178.175.128.252" blockedInCN false
        "178.175.128.253" blockedInCN false
        "178.175.128.254" blockedInCN false
        "178.175.129.251" blockedInCN false
        "178.175.129.252" blockedInCN false
        "178.175.129.253" blockedInCN false
        "178.175.129.254" blockedInCN false
        "178.175.132.19" blockedInCN false
        "178.175.132.20" blockedInCN false
        "178.175.132.21" blockedInCN false
        "178.175.132.22" blockedInCN false
    }
    hosts("api.github.com") {
        "140.82.114.5" blockedInCN false
        "140.82.114.6" blockedInCN false
        "140.82.116.5" blockedInCN false
        "140.82.116.6" blockedInCN false
        "140.82.121.5" blockedInCN false
        "140.82.121.6" blockedInCN false
        "20.200.245.245" blockedInCN false
        "20.201.28.148" blockedInCN false
        "20.205.243.168" blockedInCN false
        "20.26.156.210" blockedInCN false
        "20.27.177.116" blockedInCN false
        "20.87.245.6" blockedInCN false
        "4.237.22.34" blockedInCN false
    }
    hosts("github.com") {
        "140.82.113.4" blockedInCN false
        "140.82.114.3" blockedInCN false
        "140.82.116.4" blockedInCN false
        "140.82.121.3" blockedInCN false
        "140.82.121.4" blockedInCN false
        "20.200.245.247" blockedInCN false
        "20.201.28.151" blockedInCN false
        "20.205.243.166" blockedInCN false
        "20.26.156.215" blockedInCN false
        "20.27.177.113" blockedInCN false
        "20.87.245.0" blockedInCN false
        "4.237.22.38" blockedInCN false
    }
}
