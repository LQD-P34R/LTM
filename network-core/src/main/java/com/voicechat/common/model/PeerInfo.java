package com.voicechat.common.model;

public record PeerInfo(String id, String username, String host, int tcpPort, int udpPort) { }
