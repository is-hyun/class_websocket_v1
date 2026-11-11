package com.example.class_websocket_v1.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final ChatService chatService;
    private final SseService sseService;

    // A. 채팅 목록 화면 (폴링)
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("chats", chatService.findAll());
        return "index";
    }

    // B. 채팅 내용
    @PostMapping("/chat")
    @ResponseBody
    public ResponseEntity<Void> save(@RequestParam("msg") String msg) {
        Chat chat = chatService.save(msg);
        sseService.send(msg); // -> 브로드 캐스트
        return ResponseEntity.ok().build();
    }
}
