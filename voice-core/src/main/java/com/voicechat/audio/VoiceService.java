package com.voicechat.audio;

/**
 * One-to-one calls. Forward CALL_* control messages from the shared dispatcher
 * to
 * handleControlMessage(); NetworkGateway supports only one control callback.
 * Both peers use a call UUID as the control payload and raw 640-byte UDP
 * frames.
 */
public final class VoiceService {
}
