💭 Apache Kafka: как не терять данные под нагрузкой

Классическая проблема растущего проекта: сервис уведомлений начинает захлёбываться. В пике прилетает 10к запросов в секунду, а он обрабатывает 3к. Остальные просто теряются.

Можно горизонтально масштабировать, но это не решает проблему архитектурно. А Kafka решает.

Идея простая: Producer пишет, Consumer читает в своём темпе. Никто никого не ждёт. Никто никого не роняет.

1️⃣ Producer: отправляем событие

@Service
@RequiredArgsConstructor
public class OrderService {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void createOrder(Order order) {
        orderRepository.save(order);

        OrderEvent event = new OrderEvent(order.getId(), order.getUserId());
        kafkaTemplate.send("order-created", order.getUserId().toString(), event);
        //                  топик            ключ партиции                  payload
    }
}

Ключ партиции — важная деталь. Kafka гарантирует порядок сообщений внутри одной партиции. Если передаёшь userId как ключ — все события одного пользователя попадут в одну партицию и будут обработаны строго по порядку.

2️⃣ Consumer: читаем и обрабатываем

@Component
public class NotificationConsumer {

    @KafkaListener(
        topics = "order-created",
        groupId = "notification-group",
        concurrency = "3"  // 3 потока = читаем 3 партиции параллельно
    )
    public void handle(OrderEvent event) {
        notificationService.send(event.getUserId());
    }
}

groupId определяет логическую группу потребителей. Kafka гарантирует: одно сообщение получит ровно один инстанс внутри группы. Хочешь, чтобы событие получили оба сервиса уведомлений и аналитики? Разные groupId и каждый читает топик независимо.

3️⃣ Партиции и масштабирование

order-created (3 партиции)
├── partition-0 → consumer-instance-1
├── partition-1 → consumer-instance-2
└── partition-2 → consumer-instance-3

Не хватает скорости обработки → поднимаешь ещё инстансов. Kafka сама перераспределит партиции. Но инстансов больше, чем партиций держать смысла нет, лишние будут просто простаивать.

4️⃣ Что делать, если Consumer упал

Kafka хранит сообщения на диске (по умолчанию 7 дней). Consumer сам трекает, до какого offset он дочитал.

spring:
kafka:
consumer:
auto-offset-reset: earliest       # читать с начала, если offset не найден
enable-auto-commit: false         # коммитим offset вручную — только после успешной обработки

enable-auto-commit: false — критически важная настройка. Если Consumer упал в середине обработки, он перечитает сообщения с последнего закоммиченного offset. При true — offset уже сдвинулся, сообщение потеряно.

5️⃣ Dead Letter Topic: что делать с ядовитыми сообщениями

Иногда одно сообщение падает раз за разом: битые данные, баг в логике. Consumer уходит в бесконечный retry и встаёт колом.

@Bean
public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
var backoff = new FixedBackOff(1000L, 3); // 3 попытки с паузой 1с
return new DefaultErrorHandler(recoverer, backoff);
}

После 3 неудачных попыток сообщение уедет в топик order-created.DLT. Основной поток не заблокирован, разбираешься с проблемой отдельно.

📌 Когда Kafka, а когда нет

Нужен ответ прямо сейчас → REST
Получатель может быть недоступен → Kafka
Один источник и много потребителей → Kafka
Аудит и история событий → Kafka
Простой CRUD без нагрузки → REST

Kafka не серебряная пуля. Она добавляет операционную сложность: нужно думать об idempotency, порядке сообщений, мониторинге lag у Consumer-групп. Но когда система начинает терять данные под нагрузкой, цена этой сложности оправдана.

══════ Навигация ══════
Вакансии • Задачи • Собесы

🐸 Библиотека джависта

#CoreJava