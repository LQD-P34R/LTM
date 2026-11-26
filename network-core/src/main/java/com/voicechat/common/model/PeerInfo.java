package com.voicechat.common.model;

/** Thông tin một peer: định danh, tên hiển thị và địa chỉ/cổng dùng để liên lạc. */
public record PeerInfo(String id, String username, String host, int tcpPort, int udpPort) { }
