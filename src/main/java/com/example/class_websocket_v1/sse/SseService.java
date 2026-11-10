package com.example.class_websocket_v1.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/*
 * SSE
 * 브라우저가 한 번 연결을 열어 두면 서버가 그 연결로 메시지를 계속 밀어 넣음
 * 방향은 서버 --> 브라우저 의 단방향
 * 브라우저가 별도 HTTP 요청을 보낼 때는 새로운 요청을 만들어야 함
 */
@Slf4j
@Service
public class SseService {

    // 연결된 탭마다 SseEmitter 하나가 매핑
    private final List<SseEmitter> emitterList = new CopyOnWriteArrayList<>();

    // 연결 유지 시간 (10분)
    private static final long TIMEOUT = 10 * 60 * 1000L;

    // 브라우저가 연결을 요청할 때 SseEmitter 객체를 생성하여 List에 보관
    public SseEmitter connect() {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitterList.add(emitter);

        // 연결 정상 종료 시 목록에서 제외
        emitter.onCompletion(() -> emitterList.remove(emitter));

        // 시간이 지나면 목록에서 제외하고 연결을 마무리
        emitter.onTimeout(() -> {
            emitterList.remove(emitter);
            emitter.complete();
        });

        // 네트워크 오류 등으로 연결이 끊기면 목록에서 제외
        emitter.onError(e -> emitterList.remove(emitter));

        // 연결 확인 이벤트를 브라우저 측으로 즉시 한 번 보냄.
        // 첫 데이터가 전송될 때 응답 헤더도 함께 전송되어 브라우저의 onopen 실행
        // 아무것도 전송하지 않은 채 TIMEOUT 이 지나면 스프링이 503 응답으로 종료시키므로 한 건은 먼저 보내둠.
        try {
            emitter.send(SseEmitter.event().name("connect").data("연결 성공"));
        } catch (IOException e) {
            // throw new RuntimeException(e);
            emitterList.remove(emitter);
        }

        log.info("SSE 연결 추가. 현재 연결 수 : {}", emitterList.size());

        return emitter; // --> 웹 브라우저의 JS 측 코드로 던짐
    }

    // 연결된 모든 브라우저에 "chat" 이벤트를 통해 메시지를 전송
    public void send(String msg) {

        for (SseEmitter emitter : emitterList) {

            try {
                emitter.send(SseEmitter.event().name("chat").data(msg));
            } catch (IOException e) {
                // 이미 끊긴 연결은 전송에 실패하므로 목록에서 제외
                emitterList.remove(emitter);
            }
        }
    }

}
