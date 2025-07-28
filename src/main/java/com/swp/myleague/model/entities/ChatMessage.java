package com.swp.myleague.model.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ChatMessage {
    private String sender;
    private String content;
    private String timestamp;
    private String userId;
}

