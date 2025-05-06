package edu.kangwon.university.taxicarpool.chattingpractice;

import java.time.Instant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Chat {
    private String sender;
    private String message;
    private Instant timestamp;

}
