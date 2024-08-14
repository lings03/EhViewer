package com.hippo.ehviewer

import com.hippo.ehviewer.client.hosts
import com.hippo.ehviewer.client.hostsDsl

val builtInHosts = hostsDsl {
    hosts("ehgt.org", "gt0.ehgt.org", "gt1.ehgt.org", "gt2.ehgt.org", "gt3.ehgt.org", "ul.ehgt.org") {
        "37.48.89.44" blockedInCN false
        "81.171.10.48" blockedInCN false
        "178.162.139.24" blockedInCN false
        "2001:1af8:4700:a062:8::47de" blockedInCN false
    }
    hosts("e-hentai.org", "repo.e-hentai.org") {
        "104.20.18.168" blockedInCN false
        "104.20.19.168" blockedInCN false
        "172.67.2.238" blockedInCN true
        "178.162.139.11" blockedInCN false
        "178.162.139.12" blockedInCN false
        "178.162.139.13" blockedInCN false
        "178.162.139.14" blockedInCN false
        "178.162.139.15" blockedInCN false
        "178.162.139.16" blockedInCN false
        "178.162.139.33" blockedInCN false
        "178.162.139.34" blockedInCN false
        "178.162.139.36" blockedInCN false
        "178.162.145.131" blockedInCN false
        "178.162.145.132" blockedInCN false
        "178.162.145.152" blockedInCN false
        "37.48.89.1" blockedInCN false
        "37.48.89.13" blockedInCN false
        "37.48.89.14" blockedInCN false
        "37.48.89.15" blockedInCN false
        "37.48.89.2" blockedInCN false
        "37.48.89.20" blockedInCN false
        "37.48.89.25" blockedInCN false
        "37.48.89.26" blockedInCN false
        "37.48.89.3" blockedInCN false
        "81.171.10.49" blockedInCN false
        "81.171.10.51" blockedInCN false
        "81.171.10.53" blockedInCN false
    }
    hosts("forums.e-hentai.org") {
        "94.100.18.243" blockedInCN false
        "104.20.18.168" blockedInCN false
        "104.20.19.168" blockedInCN false
        "172.67.2.238" blockedInCN true
    }
    hosts("api.e-hentai.org") {
        "37.48.89.16" blockedInCN false
        "81.171.10.55" blockedInCN false
        "178.162.139.18" blockedInCN false
        "178.162.147.246" blockedInCN false
    }
    hosts("upload.e-hentai.org") {
        "94.100.18.247" blockedInCN false
        "94.100.18.249" blockedInCN false
    }
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
