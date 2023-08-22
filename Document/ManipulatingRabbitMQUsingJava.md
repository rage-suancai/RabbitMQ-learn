### 使用Java操作消息队列
现在我们来看看如何通过Java连接到RabbitMQ服务器并使用消息队列进行消息发送(这里一起讲解 包括Java基础版本和SpringBoot版本) 首先我们使用最基本的Java客户端连接方式:

```xml
                    <dependency>
                        <groupId>com.rabbitmq</groupId>
                        <artifactId>amqp-client</artifactId>
                        <version>5.14.2</version>
                    </dependency>
```

依赖导入之后 我们来实现一下生产者和消费者 首先是生产者 生产者负责将信息发送到消息队列:

```java
                    public static void main(String[] args) {
                        
                        // 使用ConnectionFactory来创建连接
                        ConnectionFactory factory = new ConnectionFactory();
                
                        factory.setHost("192.168.43.128"); // 设定连接信息 基操
                        factory.setPort(5672); // 注意这里写5672 是amqp协议端口
                        factory.setUsername("admin");
                        factory.setPassword("admin");
                        factory.setVirtualHost("/test");
                        
                        // 创建连接
                        try (Connection connection = factory.newConnection()) {
                
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    
                    }
```

这里我们可以直接再程序中定义并创建消息队列(实际上是和我们在管理页面创建一样的效果) 客户端需要通过连接创建一个新的通道(Channel) 同一个连接下可以有很多个通道 这样就不用创建很多个连接也能支持分开发送了

```java
                    try (Connection connection = factory.newConnection();
                         Channel channel = connection.createChannel()) { // 通过Connection创建新的Channel
                        
                         // 声明队列 如果此队列不存在 会自动创建
                         channel.queueDeclare("yyds", false, false, false, null);
                         // 将队列绑定到交换机
                         channel.queueBind("yyds", "amq.direct", "my-yyds");
                         // 发布新的消息 注意消息需要转换为byte[]
                         channel.basicPublish("amq.direct", "my-yyds", null, "Hello RabbitMQ".getBytes());
            
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
```

其中queueDeclare方法的参数如下:
- queue: 队列的名称(默认创建后routingkey和队列名称一致)
- durable: 是否持久化
- exclusive: 是否排它 如果一个队列被声明为排它队列 该队列仅对首次声明它的连接可见 并在连接断开时自动删除 排它队列是基于Connection可见 同一个Connection的不同Channel是可以同时访问同一个连接创建的排它队列
  并且 如果一个Connection已经声明了一个排它队列 其它的Connection是不允许建立同名的排它队列的 即使该队列是持久化的 一旦Connection关闭或者客户端退出 该排它队列都会自动被删除
- autoDelete: 是否自动删除
- arguments: 设置队列的其它一些参数 这里我们暂时不需要什么其它参数

其中queueBind方法参数如下:
- queue: 需要绑定的队列名称
- exchange: 需要绑定的交换机名称
- routingkey: 不用说了吧

其中basicPublisg方法的参数如下:
- exchange: 对应的Exchange名称 我们这里就使用第二个直连交换机
- routingkey: 这里我们填写绑定时指定的routingkey 其实和之前在管理页面操作一样
- props: 其它的配置
- body: 消息本体

执行完成后 可以在管理页面中看到我们刚刚创建好的消息队列了:

<img src="https://image.itbaima.net/markdown/2023/03/08/baiDgVyoPQ65TMX.jpg"/>

并且此消息队列已经成功与amq.direct交换机进行绑定:

<img src="https://image.itbaima.net/markdown/2023/03/08/5lENjHswniC4Zg8.jpg"/>

那么现在我们的消息队列中已经存在数据了 怎么将其读取出来呢? 我们来看看如何创建一个消费者:

```java
                    public static void main(String[] args) {

                        ConnectionFactory factory = new ConnectionFactory();
                        factory.setHost("192.168.43.128");
                        factory.setPort(5672);
                        factory.setUsername("admin");
                        factory.setPassword("admin");
                        factory.setVirtualHost("/test");
                        
                        // 这里不使用try-with-resource 因为消费者是一直等待新的消息到来 然后按照 我们设定的逻辑进行处理 所以这里不能在定义完成之后就关闭连接
                        // 创建一个基本的消费者
                        channel.basicConsume("yyds", false, (s, delivery) -> {
                            System.out.println(new String(delivery.getBody()));
                            // basicAck是确认应答 第一个参数是当前的消息标签 后面的参数是: 是否批量处理消息队列中所有的消息 如果为false表示只处理当前消息
                            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                            // basicNack是拒绝应答 最后一个参数表示是否将当前消息放回队列 如果为false 那么消息就会被丢放
                            // channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                            // 跟上面一样 最后一个参数为false 只不过这里省了
                            // channel.basicReject(delivery.getEnvelope().getDeliveryTag(), false);
                        }, s -> {});
            
                    }
```

其中basicConsume方法参数如下:
- queue - 消息队列名称 直接指定
- autoAck - 自动应答 消费者从消息队列取出数据后 需要跟服务器进行确认应答 当服务器收到确认后 会自动将消息删除 如果开启自动应答 那么消息发出后会直接删除
- deliver - 消息接收后的函数回调 我们可以在回调中对消息进行处理 处理完成后 需要给服务器确认应答
- cancel - 当消费者取消订阅时进行的函数回调 这里暂时用不到

现在我们启动一下消费者 可以看到立即读取我们刚刚插入到队列中的数据:

<img src="https://image.itbaima.net/markdown/2023/03/08/rR7eThxXbufjsEo.jpg"/>

我们现在继续在消息队列中插入新的数据 这里直接在网页上进行操作就行了 同样的我们也可以在消费者端接收并进行处理

现在我们把刚刚创建好的消息队列删除

官方文档: https://docs.spring.io/spring-amqp/docs/current/reference/html/

前面我们已经完成了RabbitMQ的安装和简单使用 并且通过Java连接到服务器 现在我们来尝试在SpringBoot中整合消息队列客户端 首先是依赖:

```xml
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-amqp</artifactId>
                    </dependency>
```

接着我们需要配置RabbitMQ的地址等信息:

```yaml
                    spring:
                      rabbitmq:
                        addresses: 192.168.43.128
                        username: admin
                        password: admin
                        virtual-host: /test
```

这样我们就完成了最基本信息配置 现在我们来看一下 如何像之前一样去声明一个消息队列 我们只需要一个配置类就行了:

```java
                    @Configuration
                    public class RabbitConfiguration {
                    
                        @Bean("directExchange") // 定义交换机Bean 可以很多个
                        public Exchange exchange() {
                            return ExchangeBuilder.directExchange("amp.direct").build();
                        }
                    
                        @Bean("yydsQueue") // 定义消息队列
                        public Queue queue() {
                    
                            return QueueBuilder
                                    .nonDurable("yyds") // 非持久化类型
                                    .build();
                    
                        }
                    
                        @Bean("binding")
                        public Binding binding(@Qualifier("directExchange") Exchange exchange,
                                               @Qualifier("yydsQueue") Queue queue) {
                            
                            // 将我们刚刚定义的交换机和队列进行绑定
                            return BindingBuilder
                                    .bind(queue) // 绑定队列
                                    .to(exchange) // 到交换机
                                    .with("my-yyds") // 使用自定义的routingKey
                                    .noargs();
                    
                        }
                    
                    }
```

接着我们来创建一个生产者 这里我们直接编写在测试用例中:

```java
                    @SpringBootTest
                    class SpringCloudMqApplicationTests {
                    
                        // RabbitTemplate为我们封装了大量的RabbitMQ操作 已经由Starter提供 因此直接注入使用即可
                        @Resource
                        RabbitTemplate template;
                    
                        @Test
                        void publisher() {
                            // 使用convertAndSend方法一步到位 参数基本和之前是一样的
                            // 最后一个消息本体可以是Object类型 真是大大的方便
                            template.convertAndSend("amq.direct", "my-yyds", "Hello World!");
                        }
                    
                    }
```

现在我们来运行一下这个测试用例:

<img src="https://image.itbaima.net/markdown/2023/03/08/UxVemu9B2cGifWv.jpg"/>

可以看到后台自动声明了我们刚刚定义好的消息队列和交换机以及对应的绑定关系 并且我们的数据也是成功插入到消息队列中:

<img src="https://image.itbaima.net/markdown/2023/03/08/RjY4JUn7v9pmryx.jpg"/>

现在我们再来看看如何创建一个消费者 因为消费者实际上就是一直等待消息然后进行处理的角色 这里我们只需要创建一个监听器就行了 它会一直等待消息到来然后再进行处理:

```java
                    @Component // 注册为Bean
                    public class TestListener {
                    
                        @RabbitListener(queues = "yyds") // 定义此方法为队列yyds的监听器 一旦监听器到新的消息 就会接收并处理
                        public void test(Message message) {
                            System.out.println(new String(message.getBody()));
                        }
                    
                    }
```

接着我们启动服务器:

<img src="https://image.itbaima.net/markdown/2023/03/08/ZjRs8u2cHbBEOaW.jpg"/>

可以看到控制台成功输出了我们之前放入队列的消息 并且管理页面中也显示此消费者已经连接了:

<img src="https://image.itbaima.net/markdown/2023/03/08/RwUFdgqXlDKk7AI.jpg"/>

接着我们再通过管理页面添加新的消息看看 也是可以正常进行接受的

当然 如果我们需要确保消息能够被消费者接收并处理 然后得到消费者的反馈 也是可以的:

```java
                    @Test
                    void publisher() {
    
                        // 会等待消费者消费然后返回响应结果
                        Object res = template.convertSendAndReceive("amq.direct", "my-yyds", "Hello World!");
                        System.out.println("收到消费者响应: " + res);
                        
                    }
```

消费者这边只需要返回一个对应的结果即可:

```java
                    @RabbitListener(queues = "yyds")
                    public String receiver(String data) {
                
                        System.out.println("一号消息队列监听器 " + data);
                        return "收到";
                
                    }
```

测试没有问题:

<img src="https://image.itbaima.net/markdown/2023/03/08/OkV6zN9PJRlwnQF.jpg"/>

那么如果我们需要直接接收一个JSON格式的消息 并且希望直接获取到实体类呢?

```java
                    @Data
                    public class User {
    
                        int id;
                        String name;
                        
                    }
```

```java
                    @Configuration
                    public class RabbitConfiguration {
    
                      	...
                    
                        @Bean("jacksonConverter") // 直接创建一个用于JSON转换的Bean
                        public Jackson2JsonMessageConverter converter(){
                            return new Jackson2JsonMessageConverter();
                        }
                        
                    }
```

接着我们只需要指定转换器就可以了:

```java
                    @Component
                    public class TestListener {
                    
                        // 指定messageConverter为我们刚刚创建的Bean名称
                        @RabbitListener(queues = "yyds", messageConverter = "jacksonConverter")
                        public void receiver(User user){ // 直接接收User类型
                            System.out.println(user);
                        }
                        
                    }
```

现在我们直接在管理页面发送:

```json
                    {"id":1,"name":"LB"}
```

!

<img src="https://image.itbaima.net/markdown/2023/03/08/3dXbs5naViUMrDO.jpg"/>

可以看到成功完成了转换 并输出了用户信息:

<img src="https://image.itbaima.net/markdown/2023/03/08/aM8SCL12hkKynUu.jpg"/>

同样的 我们也可以直接发送User 因为我们刚刚已经配置了Jackson2JsonMessageConverter为Bean 所以直接使用就可以了:

```java
                    @Test
                    void publisher() {
                        template.convertAndSend("amq.direct", "yyds", new User());
                    }
```

可以看到后台的数据类型为:

<img src="https://image.itbaima.net/markdown/2023/03/08/xVSpC7KHE1RyOk6.jpg"/>

<img src="https://image.itbaima.net/markdown/2023/03/08/Q9tBuprGwfleNLZ.jpg"/>

这样 我们就通过SpringBoot实现了RabbitMQ的简单使用