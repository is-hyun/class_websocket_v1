package com.example.class_websocket_v1.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SseController {

    private final SseService sseService;

    // !중요!
    /*
     * 브라우저 EventSource가 연결하는 주소
     * 응답 타입 text/event-stream 은 브라우저가 해당 응답을 "이벤트 스트림" 으로 해석하게 하는 표시
     * 이 요청은 응답이 바로 종료되지 않고, 서버가 메시지를 전송할 때까지 열린 채로 유지
     */
    @GetMapping(value = "/sse/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect() {
        return sseService.connect();
    }
}
