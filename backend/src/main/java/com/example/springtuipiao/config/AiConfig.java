package com.example.springtuipiao.config;

import com.alibaba.cloud.ai.memory.jdbc.JdbcChatMemoryRepository;
import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemoryRepository;
import com.example.springtuipiao.service.BookingTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;

import javax.sql.DataSource;
import java.util.List;

/**
 * @Description TODO
 * @Author lijiar
 * @Date 2025/11/25 0:43
 */
@Slf4j
@Configuration
public class AiConfig {

    // In the real world, ingesting documents would often happen separately, on a CI
    // server or similar.
    // CommandLineRunner 项目启动后自动运行

    @Bean
    CommandLineRunner ingestTermOfServiceToVectorStore(
            VectorStore vectorStore,
            @Value("classpath:rag/terms-of-service.txt") Resource termsOfServiceDocs
    ) {

        return args -> {
            // Ingest the document into the vector store
            /*
             * 1、文档读取TextReader 读取 resources/rag/terms-of-service.txt 文件内容
             * 2、TokenTextSplitter 按token长度切分文本（避免大文本超出模型限制）
             * 3、向量化存储 通过 VectorStore.write() 将文本向量存入内存（后续可用于RAG检索）
             */

           // vectorStore.write(new TokenTextSplitter().transform(new TextReader(termsOfServiceDocs).read()));

            TextReader textReader = new TextReader(termsOfServiceDocs);
            List<Document> documents = textReader.get();
            List<Document> apply = new TokenTextSplitter().apply(documents);
            vectorStore.add(apply);

            // 相似性搜索检测
            vectorStore.similaritySearch("Cancelling Bookings").forEach(doc -> {
                log.info("Similar Document: {}", doc.getText());
            });
        };
    }

    /**
     * 提供基于内存的向量存储（SimpleVectorStore）
     * <p>
     * 依赖 EmbeddingModel（自动注入，Alibaba的嵌入模型）
     * @param embeddingModel
     * @return
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {

        return SimpleVectorStore.builder(embeddingModel).build();
    }




    @Bean
    public JdbcChatMemoryRepository chatMemoryRepository(DataSource dataSource) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return MysqlChatMemoryRepository
                .mysqlBuilder()
                .jdbcTemplate(jdbcTemplate)
                .build();
    }
    /**
     * 存储多轮对话历史
     * 实现上下文感知的连续对话
     * @return
     */
    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {

        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository) // 改动之处
                .maxMessages(20)
                .build();

    }

    @Bean
    public MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

    /**
     * 提供可自定义的HTTP客户端（用于调用外部API）
     * @return
     */
    @Bean
    @ConditionalOnMissingBean // @ConditionalOnMissingBean 是一个 条件化配置注解，意思是："只有当指定的Bean不存在时，才创建这个Bean"
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }


    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
                                 MessageChatMemoryAdvisor messageChatMemoryAdvisor,
                                 BookingTools bookingTools,
                                 VectorStore vectorStore) {
        return builder
                .defaultSystem("""
                        您是“Funnair”航空公司的客户聊天支持代理。请以友好、乐于助人且愉快的方式来回复。
						您正在通过在线聊天系统与客户互动。
						您能够支持已有机票的预订详情查询、机票日期改签、机票预订取消等操作，其余功能将在后续版本中添加，如果用户问的问题不支持请告知详情。
						在提供有关机票预订详情查询、机票日期改签、机票预订取消等操作之前，您必须始终从用户处获取以下信息：预订号、客户姓名。
						在询问用户之前，请检查消息历史记录以获取预订号、客户姓名等信息，尽量避免重复询问给用户造成困扰。
						在更改预订之前，您必须确保条款允许这样做。
						如果更改需要收费，您必须在继续之前征得用户同意。
						使用提供的功能获取预订详细信息、更改预订和取消预订。
						如果需要，您可以调用相应函数辅助完成。
						请讲中文。不要生成表情。

						今天的日期是 {current_date}.
                        """)
                .defaultAdvisors(messageChatMemoryAdvisor,new QuestionAnswerAdvisor(vectorStore) )
                .defaultTools(bookingTools)

                .build();
        // QuestionAnswerAdvisor 详解
        //QuestionAnswerAdvisor 是 Spring AI 框架中的一个核心组件，用于在 AI 对话过程中自动进行知识检索和上下文增强。
    }
}
