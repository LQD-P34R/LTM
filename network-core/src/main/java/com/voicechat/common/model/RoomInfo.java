package com.voicechat.common.model;

/** Thông tin phòng nhóm: mã phòng, tên hiển thị và ID peer sở hữu phòng. */
public record RoomInfo(String id, String name, String ownerPeerId) { }
