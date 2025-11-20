package com.huacai.ai.controller;

import com.huacai.ai.service.DialogueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private DialogueService dialogueService;

    /**
     * 对话接口 - 支持流式输出（像打字一样逐字显示）
     * 这个接口实现了类似ChatGPT的流式对话效果，回答会一个字一个字地慢慢显示出来
     * 而不是等整个回答生成完毕再一次性显示
     *
     * @param memoryId 记忆ID - 相当于对话的"会话编号"，用来区分不同的对话
     *                 比如：用户A的对话记忆是1，用户B的对话记忆是2，互不干扰
     * @param message 用户发送的消息内容 - 比如"你好，请介绍一下你自己"
     * @return 返回一个数据流，包含AI逐字回复的内容
     */
    @GetMapping("/dialogue")
    public Flux<ServerSentEvent<String>> dialogue(int memoryId, String message) {
        // 调用对话服务的流式聊天方法，获取AI的回复数据流
        return dialogueService.sseChat(memoryId, message)
                // 将每个回复片段包装成ServerSentEvent格式，方便前端接收
                .map(chunk -> ServerSentEvent
                        .<String>builder()  // 创建一个SSE事件构建器
                        .data(chunk)        // 设置实际的数据内容（AI回复的文本片段）
                        .build());          // 构建完成的事件对象
    }

}
