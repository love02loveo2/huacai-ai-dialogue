package com.huacai.ai.config;

import dev.langchain4j.data.document.Document;
// import dev.langchain4j.data.document.loader.FileSystemDocumentLoader; // ❌ 不再使用（无法指定编码）
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Configuration
public class RagConfig {

    // 文本向量化模型：负责把文字转换成计算机能理解的数字向量
    // 比如把"苹果"转换成[0.1, 0.5, -0.3, ...]这样的数字序列
// ====================== 使用自动注入替代手动创建 Bean ======================
    // ✅ 推荐方式：由 langchain4j-spring-boot-starter 自动创建并注册 EmbeddingModel
    // application.yml 中已配置：
    // langchain4j:
    //   community:
    //     dashscope:
    //       embedding-model:
    //         model-name: text-embedding-v2
    //         api-key: ${DASHSCOPE_API_KEY}
    @Autowired
    private EmbeddingModel aliEmbeddingModel;// 自动从 Spring 容器注入
    // 向量数据库：用来存储和搜索这些文本向量
    // 想象成一个专门存放文字"指纹"的特殊仓库

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    // ====================== 注册默认向量存储（内存版） ======================
    // 你可以改成 PGVector、Milvus 等持久化实现
//    @Bean
//    public EmbeddingStore<TextSegment> embeddingStore() {
//        return new InMemoryEmbeddingStore<>();
//    }

    /**
     * 创建文档检索控制器
     *
     * 这个系统的工作流程：
     * 1. 读取文档 → 2. 切分文档 → 3. 转换成向量 → 4. 存入向量库 → 5. 搭建检索器
     *
     * 当用户提问时，系统会把问题也转换成向量，然后在向量库中寻找最相似的文档片段来回答问题
     */

    // 从配置文件读取文档路径，支持 file: 或 classpath:
    @Value("${huacai.ai.rag-docs-path:classpath:docs}")
    private String ragDocsPath;

    @Bean
    public ContentRetriever contentRetriever() throws IOException {
        // 🗂️ 第一步：加载知识库文档
        // 从resources/docs文件夹读取所有文档文件
        // 这些文档就是AI的"知识教科书"

        // ✅ 支持两种路径：file:/path/to/docs 或 classpath:docs
        Path docsPath;

        if (ragDocsPath.startsWith("classpath:")) {
            // 处理 classpath 路径（仅用于开发环境）
            String resourcePath = ragDocsPath.substring("classpath:".length());
            Resource resource = new ClassPathResource(resourcePath);
            if (!resource.exists()) {
                throw new IllegalStateException("知识库目录不存在: " + ragDocsPath + "，请检查 src/main/resources/" + resourcePath + " 是否存在");
            }
            try {
                docsPath = resource.getFile().toPath(); // 开发时可用
            } catch (Exception e) {
                throw new IllegalStateException(
                        "无法加载 classpath 路径（可能在 jar 包中运行）: " + ragDocsPath +
                                "，请改用 file: 开头的外部路径，例如 file:D:/huacai/docs", e);
            }
        } else if (ragDocsPath.startsWith("file:")) {
            // 处理文件系统路径（推荐用于生产）
            String filePath = ragDocsPath.substring(5);
            docsPath = Paths.get(filePath);
        } else {
            // 默认当作绝对路径处理
            docsPath = Paths.get(ragDocsPath);
        }

        // 检查路径是否存在且为目录
        if (!Files.exists(docsPath)) {
            throw new IllegalStateException("文档路径不存在: " + docsPath.toAbsolutePath());
        }
        if (!Files.isDirectory(docsPath)) {
            throw new IllegalStateException("文档路径不是目录: " + docsPath.toAbsolutePath());
        }

        // ✅ 自定义加载 .txt 和 .md 文件，强制使用 UTF-8 编码（解决乱码）
        List<Document> documents = loadTextAndMarkdownDocuments(docsPath);

        // ✅ 添加日志：输出加载的文档数量
        System.out.println("📄 成功加载 " + documents.size() + " 个文档，来源: " + docsPath.toAbsolutePath());

        // ✂️ 第二步：准备文档切割器
        // 为什么要切割文档？
        // - 长文档不易处理：就像读书时我们不会整本书背，而是分章节学习
        // - 提高检索精度：只返回最相关的段落，而不是整篇文档
        DocumentByParagraphSplitter paragraphSplitter = new DocumentByParagraphSplitter(
                1000,  // 每个文本块最多1000个字符（约200-300字）
                200    // 块之间重叠200字符，避免把完整的意思拦腰截断
        );

        // 🏭 第三步：创建文档处理流水线
        // 这个流水线负责：切割文档 → 添加标签 → 转换成向量 → 存入数据库
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(paragraphSplitter)  // 使用段落切割器
                // 给每段文本加上"出处标签"，这样回答时会显示信息来源
                // 比如："员工手册.pdf\n员工请假需提前3天申请..."
                .textSegmentTransformer(textSegment -> TextSegment.from(
                        "📄 " + textSegment.metadata().getString("file_name") + "\n" + textSegment.text(),
                        textSegment.metadata()
                ))
                .embeddingModel(aliEmbeddingModel)  // 文本转向量模型
                .embeddingStore(embeddingStore)     // 向量存储位置
                .build();

        // 📥 第四步：处理并存储所有文档
        // 这步相当于让AI"学习"所有文档内容
        // 文档被切割成小块，转换成向量后存入向量数据库
        ingestor.ingest(documents);

        // 🔍 第五步：创建智能检索器
        // 当用户提问时，这个检索器负责：
        // 1. 把用户问题转换成向量
        // 2. 在向量库中搜索最相似的文档片段
        // 3. 返回质量最高的结果给AI参考
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)     // 指定搜索的向量库
                .embeddingModel(aliEmbeddingModel)  // 问题转向量用的模型
                .maxResults(3)      // 最多返回3个相关结果（兼顾准确性和信息量）
                .minScore(0.65)     // 相似度门槛：65%（过滤掉不相关的内容）
                .build();

        return contentRetriever;
    }

    // ✅ 仅加载 .txt 和 .md 文件，并使用 UTF-8 编码读取（解决乱码）
    private List<Document> loadTextAndMarkdownDocuments(Path docsDir) throws IOException {
        List<Document> documents = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(docsDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return name.endsWith(".txt") || name.endsWith(".md");
                    })
                    .forEach(file -> {
                        try {
                            // ✅ 关键：使用 UTF-8 读取内容
                            String content = Files.readString(file, StandardCharsets.UTF_8);
                            Document doc = Document.from(content);
                            doc.metadata().put("file_name", file.getFileName().toString());
                            documents.add(doc);
                        } catch (Exception e) {
                            System.err.println("❌ 读取文件失败（跳过）: " + file + " - " + e.getMessage());
                        }
                    });
        }

        return documents;
    }
}