AI知识库对话小助手
项目简介
AI对话小助手结合了人工智能技术和检索增强生成（RAG）架构，为用户提供智能化的个性服务。通过集成阿里云通义千问大模型和向量数据库技术，该系统能够理解用户问题，从知识库中检索相关信息，并生成准确、个性化的回答。
核心功能
1. 智能对话系统
● 基于阿里云通义千问 qwen-max 模型构建
● 支持自然语言交互，理解用户，学习相关问题
● 提供个性化的输出
2. 流式响应输出
● 实现类似ChatGPT的打字机效果
● 逐字逐句实时显示AI回答，提升用户体验
● 通过Server-Sent Events (SSE)技术实现
3. 检索增强生成 (RAG)
● 集成向量数据库和文本嵌入模型 (text-embedding-v4)
● 能够从本地知识库文档中检索相关信息
● 提高回答的准确性和专业性
4. 对话记忆管理
● 支持多轮对话上下文管理
● 通过memoryId区分不同用户的对话会话
● 每个会话保留最近10条对话记录
技术架构
后端技术栈
● Spring Boot 3.5.0
● Java 17
● LangChain4j AI框架
● 阿里云DashScope平台
● Maven项目管理
前端技术栈
● Vue 3
● Element Plus UI组件库
● Vite构建工具
核心AI组件
● 聊天模型: qwen-max
● 嵌入模型: text-embedding-v4
● 流式聊天模型: qwen-max
项目特色
1. 知识库检索能力
● 自动加载 resources/docs 目录下的文档
● 智能分段处理（每段最多1000字符，重叠200字符）
● 为每个文本段添加来源标识
● 相似度阈值过滤（最小相似度0.65）
2. 多种交互方式
1. 普通对话：基础问答功能
2. 报告生成：结构化信息输出
3. RAG对话：基于知识库的精准问答
4. 流式对话：实时逐字显示的交互体验
效果：
<img width="997" height="515" alt="image" src="https://github.com/user-attachments/assets/e4fa2e34-74bf-4d55-9fd0-5e7838351495" />
<img width="1186" height="537" alt="image" src="https://github.com/user-attachments/assets/35dec67e-ce26-4371-831d-e1fd80f86e07" />
<img width="983" height="496" alt="image" src="https://github.com/user-attachments/assets/4b578c68-b573-4358-b7bf-81cf0eb7fd3c" />



