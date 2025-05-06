package edu.kangwon.university.taxicarpool.chattingpractice;

import edu.kangwon.university.taxicarpool.chattingpractice.ChatService.ChatMessage;
import edu.kangwon.university.taxicarpool.chattingpractice.ChatService.MessageType;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;


    public ChatController(SimpMessagingTemplate messagingTemplate, ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    /**
     * 1) 채팅방 입장
     */
    @MessageMapping("/chat.enter/{roomId}")
    public void enterRoom(@DestinationVariable String roomId, ChatMessage incoming) {
        boolean joined = chatService.enterRoom(roomId, incoming.getSender());
        ChatMessage msg;
        if (joined) {
            // 입장 성공 시스템 메시지
            msg = new ChatMessage(
                roomId,
                incoming.getSender(),
                incoming.getSender() + "님이 입장하였습니다.",
                MessageType.ENTER,
                LocalDateTime.now()
            );
        } else {
            // 입장 실패(방 가득 참)
            msg = new ChatMessage(
                roomId,
                "SYSTEM",
                incoming.getSender() + "님, 방이 가득 찼습니다.",
                MessageType.ERROR,
                LocalDateTime.now()
            );
        }
        // 구독 중인 클라이언트에게 발행
        messagingTemplate.convertAndSend("/sub/chatroom." + roomId, msg);
    }

    /**
     * 2) 채팅 메시지 전송
     */
    @MessageMapping("/chat.send/{roomId}")
    public void sendMessage(@DestinationVariable String roomId, ChatMessage incoming) {
        boolean stored = chatService.sendMessage(roomId, incoming.getSender(),
            incoming.getMessage());
        ChatMessage msg;
        if (stored) {
            // 일반 채팅 메시지
            msg = new ChatMessage(
                roomId,
                incoming.getSender(),
                incoming.getMessage(),
                MessageType.TALK,
                LocalDateTime.now()
            );
        } else {
            // 메시지 전송 실패 (방이 없음)
            msg = new ChatMessage(
                roomId,
                "SYSTEM",
                "메시지 전송 실패: 방을 찾을 수 없습니다.",
                MessageType.ERROR,
                LocalDateTime.now()
            );
        }
        messagingTemplate.convertAndSend("/sub/chatroom." + roomId, msg);
    }

    /**
     * 3) 채팅방 퇴장
     */
    @MessageMapping("/chat.leave/{roomId}")
    public void leaveRoom(@DestinationVariable String roomId, ChatMessage incoming) {
        chatService.leaveRoom(roomId, incoming.getSender());
        ChatMessage msg = new ChatMessage(
            roomId,
            incoming.getSender(),
            incoming.getSender() + "님이 퇴장하였습니다.",
            MessageType.LEAVE,
            LocalDateTime.now()
        );
        messagingTemplate.convertAndSend("/sub/chatroom." + roomId, msg);
    }
}