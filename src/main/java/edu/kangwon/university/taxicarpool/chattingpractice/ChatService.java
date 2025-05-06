package edu.kangwon.university.taxicarpool.chattingpractice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    // 최대 4명 제한
    private static final int MAX_USERS = 4;

    // roomId → 참가자 집합
    private final Map<String, Set<String>> roomParticipants = new ConcurrentHashMap<>();

    // roomId → 메시지 히스토리 리스트
    private final Map<String, List<ChatMessage>> roomMessages = new ConcurrentHashMap<>();

    /**
     * 방에 입장 시도
     *
     * @return true: 입장 성공, false: 인원 초과 등 입장 실패
     */
    public boolean enterRoom(String roomId, String userId) {
        // 참가자 집합 생성 또는 조회
        Set<String> participants = roomParticipants
            .computeIfAbsent(roomId, id -> ConcurrentHashMap.newKeySet());

        // 최대 인원 체크
        if (participants.size() >= MAX_USERS) {
            // 실패 메시지도 히스토리에 기록할 수 있음
            addSystemMessage(roomId, userId + "님, 방이 가득 찼습니다.", MessageType.ERROR);
            return false;
        }

        // 참여자 추가
        boolean added = participants.add(userId);
        if (added) {
            addSystemMessage(roomId, userId + "님이 입장하였습니다.", MessageType.ENTER);
        }
        return added;
    }

    /**
     * 방에서 퇴장
     */
    public void leaveRoom(String roomId, String userId) {
        Set<String> participants = roomParticipants.get(roomId);
        if (participants != null && participants.remove(userId)) {
            addSystemMessage(roomId, userId + "님이 퇴장하였습니다.", MessageType.LEAVE);
        }
    }

    /**
     * 채팅 메시지 전송 요청 처리
     *
     * @return true if stored, false if 방이 없음
     */
    public boolean sendMessage(String roomId, String userId, String content) {
        List<ChatMessage> history = roomMessages.computeIfAbsent(roomId, id -> new ArrayList<>());
        if (!roomParticipants.containsKey(roomId)) {
            return false;
        }
        ChatMessage msg = new ChatMessage(
            roomId, userId, content, MessageType.TALK, LocalDateTime.now()
        );
        history.add(msg);
        return true;
    }

    /**
     * 해당 방의 메시지 히스토리 조회
     */
    public List<ChatMessage> getMessageHistory(String roomId) {
        return roomMessages.getOrDefault(roomId, Collections.emptyList());
    }

    /**
     * 해당 방의 현재 참여자 목록 조회
     */
    public Set<String> getParticipants(String roomId) {
        return roomParticipants.getOrDefault(roomId, Collections.emptySet());
    }

    // ----------------------------------------------------------------
    // 내부 헬퍼: 시스템(입장/퇴장/오류) 메시지를 히스토리에 추가
    private void addSystemMessage(String roomId, String content, MessageType type) {
        List<ChatMessage> history = roomMessages.computeIfAbsent(roomId, id -> new ArrayList<>());
        history.add(new ChatMessage(roomId, "SYSTEM", content, type, LocalDateTime.now()));
    }

    // ----------------------------------------------------------------
    // 테스트용 ChatMessage DTO
    public static class ChatMessage {

        private String roomId;
        private String sender;
        private String message;
        private MessageType type;
        private LocalDateTime timestamp;

        public ChatMessage(String roomId, String sender, String message, MessageType type,
            LocalDateTime timestamp) {
            this.roomId = roomId;
            this.sender = sender;
            this.message = message;
            this.type = type;
            this.timestamp = timestamp;
        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public MessageType getType() {
            return type;
        }

        public void setType(MessageType type) {
            this.type = type;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }

    public enum MessageType {
        ENTER,   // 입장
        TALK,    // 채팅
        LEAVE,   // 퇴장
        ERROR    // 입장 실패 등
    }
}
