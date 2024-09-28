package com.hippo.ehviewer

import com.hippo.ehviewer.client.hosts
import com.hippo.ehviewer.client.hostsDsl
import com.hippo.ehviewer.util.hexStringToByteArray

val echEnabledDomains = listOf(
    "exhentai.org",
    "e-hentai.org",
    "forums.e-hentai.org",
    "testingcf.jsdelivr.net"
)
val echConfig = hexStringToByteArray("0045fe0d00415800200020ac0228faf086a9710ff521d72a7366df19be521fc23350fd2554c76e8cb66d4f0004000100010012636c6f7564666c6172652d6563682e636f6d0000")

val builtInHosts = hostsDsl {
    hosts("ehgt.org", "gt0.ehgt.org", "gt1.ehgt.org", "gt2.ehgt.org", "gt3.ehgt.org", "ul.ehgt.org") {
        "109.236.85.28" blockedInCN false
        "178.162.139.24" blockedInCN false
        "2001:1af8:4700:a062:8::47de" blockedInCN false
        "2a00:7c80:0:123::3a85" blockedInCN false
        "2a00:7c80:0:12d::38a1" blockedInCN false
        "2a00:7c80:0:13b::37a4" blockedInCN false
        "37.48.89.44" blockedInCN false
        "62.112.8.21" blockedInCN false
        "81.171.10.48" blockedInCN false
        "89.39.106.43" blockedInCN false
    }
    hosts("e-hentai.org", "repo.e-hentai.org") {
        "104.20.18.168" blockedInCN false
        "104.20.19.168" blockedInCN false
        "172.67.2.238" blockedInCN false
        "178.162.139.33" blockedInCN false
        "178.162.139.34" blockedInCN false
        "178.162.139.36" blockedInCN false
        "178.162.145.131" blockedInCN false
        "178.162.145.132" blockedInCN false
        "178.162.145.152" blockedInCN false
    }
    hosts("forums.e-hentai.org") {
        "104.20.18.168" blockedInCN false
        "104.20.19.168" blockedInCN false
        "172.67.2.238" blockedInCN false
        "94.100.18.243" blockedInCN false
    }
    hosts("api.e-hentai.org") {
        "104.20.18.168" blockedInCN false
        "104.20.19.168" blockedInCN false
        "172.67.2.238" blockedInCN false
        "212.7.200.104" blockedInCN false
        "212.7.202.51" blockedInCN false
        "37.48.81.204" blockedInCN false
        "37.48.92.161" blockedInCN false
        "5.79.104.110" blockedInCN false
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
