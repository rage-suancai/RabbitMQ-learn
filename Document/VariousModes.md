### 工作队列模式
注意: XX模式只是一种设计思路 并不是指的具体的某种实现 可以理解为实现XX模式需要怎么去写

前面我们了解了最简单的一个消费者一个生产者的模式 接着我们来了解一下一个生产者多个消费者的情况:

<img src="https://image.itbaima.net/markdown/2023/03/08/8AR4H5LbOCrXZmu.jpg"/>

实际上这种模式就非常合适多个工人等待新的任务到来的场景 我们的任务有很多个 一个一个丢进消息队列 而此时工人有很多个
那么我们就可以将这些任务分配给各个工人 让他们各自负责一些任务 并且做的快的工人还可以多完成一些(能者多劳)

非常简单 我们需要创建两个监听器即可:

```java
                    @Component
                    public class TestListener {
    
                        @RabbitListener(queues = "yyds")
                        public void receiver1(String data){ // 这里直接接收String类型的数据
                            System.out.println("一号消息队列监听器 " + data);
                        }
                    
                        @RabbitListener(queues = "yyds")
                        public void receiver2(String data){
                            System.out.println("二号消息队列监听器 " + data);
                        }
                        
                    }
```

可以看到我们发送消息时 会自动进行轮询分发:

<img src="https://image.itbaima.net/markdown/2023/03/08/YgibmNxD9qtHajQ.jpg"/>

那么如果我们一开始就在消息队列中放入一部分消息在开启消费者呢?

<img src="https://image.itbaima.net/markdown/2023/03/08/Rv6YkDSTPl83Hmo.jpg"/>

可以看到 如果是一开始就存在消息 会被一个消费者一次性全部消耗 这是因为我们没有对消费者的PrefetchCount(预获取数量 一次性获取消息的最大数量)进行限制
也就是说我们现在希望的是消费者一次只拿一个消息 而不是将所有的消息全部都获取

<img src="https://image.itbaima.net/markdown/2023/03/08/UNEniupt5cRHz93.jpg"/>

因此我们需要对这个数量进行一些配置 这里我们需要在配置类中定义一个自定义的ListenerContainerFactory 可以在这里设定消费者Channel的PrefetchCount的大小:

```java
                    @Resource
                    private CachingConnectionFactory connectionFactory;
                    
                    @Bean(name = "listenerContainer")
                    public SimpleRabbitListenerContainerFactory listenerContainer(){
                        
                        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
                        factory.setConnectionFactory(connectionFactory);
                        factory.setPrefetchCount(1); // 将PrefetchCount设定为1表示一次只能取一个
                        return factory;
                        
                    }
```

接着我们在监听器这边指定即可:

```java
                    @Component
                    public class TestListener2 {
                    
                        @RabbitListener(queues = "yyds", containerFactory = "listenerContainer")
                        public void receiver1(String data) {
                            System.out.println("一号消息队列监听器: " + data);
                        }
                    
                        @RabbitListener(queues = "yyds", containerFactory = "listenerContainer")
                        public void receiver2(String data) {
                            System.out.println("二号消息队列监听器: " + data);
                        }
                    
                    }
```

现在我们再次启动服务器 可以看到PrefetchCount被限定为1了:

<img src="https://image.itbaima.net/markdown/2023/03/08/KgWzOUu8ry2V9Ej.jpg"/>

再次重复上述的实现 可以看到消息不会被一号消费者给全部抢走了:

<img src="https://image.itbaima.net/markdown/2023/03/08/vmAfh68GpuQXdUk.jpg"/>

当然除了去定义两个相同的监听器之外 我们也可以直接在注解中定义 比如我们现在需要10个同样的消费者:

```java
                    @Component
                    public class TestListener {
    
                        @RabbitListener(queues = "yyds",  containerFactory = "listenerContainer", concurrency = "10")
                        public void receiver(String data){
                            System.out.println("一号消息队列监听器: " + data);
                        }
                        
                    }
```

可以看到在管理页面中出现了10个消费者:

<img src="https://image.itbaima.net/markdown/2023/03/08/REl1qIaMXLTK6js.jpg"/>

至此 有关工作队列模式就讲到这里

### 发布订阅模式
前面我们已经了解了RabbitMQ客户端的一些基本操作 包括普通的消息模式 接着我们来了解一下其它的模式 首先是发布订阅模式 它支持多种方式:

<img src="https://image.itbaima.net/markdown/2023/03/08/fetLjQszH7cTZmO.jpg"/>

比如我们在阿里云买了云服务器 但是最近快到期了 那么就会给你的手机, 邮箱发送消息 告诉你需要去续费了 但是手机短信和邮件发送并不一定是同一个业务提供的
但是现在我们又希望能够都去执行 所以就可以用到发布订阅模式 简而言之就是 发布一次 消费多个

实现这种模式其实也非常简单 但是如果使用我们之前的直连交换机 肯定是不行的 我们这里需要用到另一种类型的交换机 叫做fanout(扇出)类型 这是一种广播类型 消息会被广播到所有与此交换机绑定的消息队列中

这里我们使用默认的交换机:

<img src="https://image.itbaima.net/markdown/2023/03/08/Er7RBCjm3nNJZHT.jpg"/>

这个交换机是一个fanout类型的交换机 我们就是要它就行了:

```java
                    @Configuration
                    public class RabbitConfiguration4 {
                    
                        @Bean("fanoutExchange")
                        public Exchange exchange() {
                            // 注意这里是fanoutExchange
                            return ExchangeBuilder.fanoutExchange("amq.fanout").build();
                        }
                    
                        @Bean("yydsQueue1")
                        public Queue queue1() {
                            return QueueBuilder.nonDurable("yyds1").build();
                        }
                        @Bean("binding1")
                        public Binding binding1(@Qualifier("fanoutExchange") Exchange exchange,
                                                @Qualifier("yydsQueue1") Queue queue) {
                    
                            return BindingBuilder
                                    .bind(queue)
                                    .to(exchange)
                                    .with("yyds1")
                                    .noargs();
                    
                        }
                    
                        @Bean("yydsQueue2")
                        public Queue queue2() {
                            return QueueBuilder.nonDurable("yyds2").build();
                        }
                        @Bean("binding2")
                        public Binding binding2(@Qualifier("fanoutExchange") Exchange exchange,
                                                @Qualifier("yydsQueue2") Queue queue) {
                    
                            return BindingBuilder
                                    .bind(queue)
                                    .to(exchange)
                                    .with("yyds2")
                                    .noargs();
                    
                        }
                    
                    }
```

这里我们两个队列都绑定到此交换机上 我们先启动看看效果:

<img src="https://image.itbaima.net/markdown/2023/03/08/pFXEmbv7LCMKxwq.jpg"/>

绑定没有什么问题 接着我们搞两个监听器 监听一下这两个队列:

```java
                    @Component
                    public class TestListener {
    
                        @RabbitListener(queues = "yyds1")
                        public void receiver1(String data){
                            System.out.println("一号消息队列监听器: " + data);
                        }
                    
                        @RabbitListener(queues = "yyds2")
                        public void receiver2(String data){
                            System.out.println("二号消息队列监听器: " + data);
                        }
                        
                    }
```

现在我们通过交换机发送消息 看看是不是两个监听器都会接收到消息:

<img src="https://image.itbaima.net/markdown/2023/03/08/k7V1xXyGTPKO6eb.jpg"/>

可以看到确实是两个消息队列都能够接收到此消息:

<img src="https://image.itbaima.net/markdown/2023/03/08/vhwydqXr9Ue61t4.jpg"/>

这样我们就实现了发布订阅模式

### 路由模式
路由模式实际上我们一开始就已经实现了 我们可以在绑定时指定想要的routingkey 只有生产者发送时指定了对应的routingkey才能到达对应的队列

<img src="https://image.itbaima.net/markdown/2023/03/08/52vs9bualApXGMR.jpg"/>

当然除了我们之前的一次绑定之外 同一个消息队列可以多次绑定到交换机 并且使用不同的routingkey 这样只要满足其中一个都可以被发送到此消息队列中:

```java
                    @Configuration
                    public class RabbitConfiguration5 {
                    
                        @Bean("directExchange")
                        public Exchange exchange() {
                            return ExchangeBuilder.directExchange("amq.direct").build();
                        }
                    
                        @Bean("yydsQueue")
                        public Queue queue() {
                            return QueueBuilder.nonDurable("yyds").build();
                        }
                    
                        @Bean("binding1") // 使用yyds1绑定
                        public Binding binding1(@Qualifier("directExchange") Exchange exchange,
                                                @Qualifier("yydsQueue") Queue queue) {
                    
                            return BindingBuilder
                                    .bind(queue)
                                    .to(exchange)
                                    .with("yyds1")
                                    .noargs();
                    
                        }
                        @Bean("binding2") // 使用yyds2绑定
                        public Binding binding2(@Qualifier("directExchange") Exchange exchange,
                                                @Qualifier("yydsQueue") Queue queue) {
                    
                            return BindingBuilder
                                    .bind(queue)
                                    .to(exchange)
                                    .with("yyds2")
                                    .noargs();
                    
                        }
                    
                    }
```

启动我们可以看到管理面板中出现了两个绑定关系:

<img src="https://image.itbaima.net/markdown/2023/03/08/n9NxMEsoCeWSaVL.jpg"/>

这里可以测试一下 随便使用哪个routingkey都可以

### 主题模式
实际上这种模式就是一种模糊匹配的模式 我们可以将routingkey以模糊匹配的方式去进行转发

<img src="https://image.itbaima.net/markdown/2023/03/08/z45gI7UaKmCipEL.jpg"/>

我们可以使用*或#来表示:
- *表示任意的一个单词
- #表示0个或多个单词

这里我们来测试一下:

```java
                    @Configuration
                    public class RabbitConfiguration6 {
                    
                        @Bean("topicExchange") // 这里使用预置的Topic类型交换机
                        public Exchange exchange() {
                            return ExchangeBuilder.topicExchange("amq.topic").build();
                        }
                    
                        @Bean("yydsQueue")
                        public Queue queue() {
                            return QueueBuilder.nonDurable("yyds").build();
                        }
                    
                        @Bean("binding")
                        public Binding binding(@Qualifier("topicExchange") Exchange exchange,
                                               @Qualifier("yydsQueue") Queue queue) {
                    
                            return BindingBuilder
                                    .bind(queue)
                                    .to(exchange)
                                    .with("*.test.*")
                                    .noargs();
                    
                        }
                    
                    }
```

启动项目 可以看到只要是满足通配符条件都可以成功转发到对应的消息队列:

<img src="https://image.itbaima.net/markdown/2023/03/08/aS37QitoUdf4FZ9.jpg"/>

接着我们可以在试试看#通配符

除了我们这里使用的默认主题交换机之外 还有一个叫做amq.rabbitmq.trace的交换机:

<img src="https://image.itbaima.net/markdown/2023/03/08/CWfRIwoYLjQrbpH.jpg"/>

可以看到它也是topic类型的 那么这个交换机是做什么的呢? 实际上这是用于帮助我们记录和追踪生产者和消费者使用消息队列的交换机 它是一个内部的交换机 那么如何使用呢? 首先创建一个消息队列用于接收记录:

<img src="https://image.itbaima.net/markdown/2023/03/08/s7B38pjkd4EGFLI.jpg"/>

接着我们需要在控制台将虚拟主机/test的追踪功能开启:

```shell
                    sudo rabbitmctl trace_on -p /test
```

开启后 我们将此队列绑定到上面的交换机上:

<img src="https://image.itbaima.net/markdown/2023/03/08/VsD2dYIpHhbt6R9.jpg"/>

<img src="https://image.itbaima.net/markdown/2023/03/08/EM4WKHqtyz3vLSk.jpg"/>

由于发送到此交换机上的routingkey为publish.交换机名称和deliver.队列名称 分别对应生产者投递到交换机的消息 因此这里使用#通配符进行绑定 现在我们来测试一下 比如还是往yyds队列发送消息:

<img src="https://image.itbaima.net/markdown/2023/03/08/vHKPqJFahV8y7l3.jpg"/>

可以看到在发送消息 并且消费者已经处理之后 trace队列中新增了两条消息 那么我们来看看都是些什么消息:

<img src="https://image.itbaima.net/markdown/2023/03/08/vHKPqJFahV8y7l3.jpg"/>

通过追踪 我们可以很明确地得知消息发送的交换机, routingkey, 用户等信息, 包括信息本身 同样的 消费者在取出数据时也有记录:

<img src="https://image.itbaima.net/markdown/2023/03/08/NApBodythmfjzMV.jpg"/>

我们可以明确消费者的地址, 端口, 具体操作的队列以及取出的消息信息等

到这里 我们就已经了解了三种类型的交换机

### 第四种交换机类型
通过前面的学习 我们已经介绍了三种交换机类型 现在我们来介绍一下第四种交换机类型header 它是根据头部信息来决定的
在我们发送的消息中是可以携带一些头部信息的(类似于HTTP) 我们可以根据这些头部信息来决定路由到哪一个消息队列中

```java
                    @Configuration
                    public class RabbitConfiguration {
                    
                        @Bean("headerExchange") // 注意这里返回的是HeadersExchange
                        public HeadersExchange exchange(){
                            
                            return ExchangeBuilder
                                    .headersExchange("amq.headers") // RabbitMQ为我们预置了两个 这里用第一个就行
                                    .build();
                            
                        }
                    
                        @Bean("yydsQueue")
                        public Queue queue(){
                            return QueueBuilder.nonDurable("yyds").build();
                        }
                    
                        @Bean("binding")
                        public Binding binding(@Qualifier("headerExchange") HeadersExchange exchange, // 这里和上面一样的类型
                                                @Qualifier("yydsQueue") Queue queue){
                            
                            return BindingBuilder
                                    .bind(queue)
                                    .to(exchange) // 使用HeadersExchange的to方法 可以进行进一步配置
                                    // .whereAny("a", "b").exist(); 这个是只要存在任意一个指定的头部Key就行
                                    // .whereAll("a", "b").exist(); 这个是必须存在所有指定的的头部Key
                                    .where("test").matches("hello"); // 比如我们现在需要消息的头部信息中包含test 并且值为hello才能转发给我们的消息队列
                                    // .whereAny(Collections.singletonMap("test", "hello")).match(); 传入Map也行 批量指定键值对
                            
                        }
                    }
```

现在我们来启动一下试试看:

<img src="https://image.itbaima.net/markdown/2023/03/08/NApBodythmfjzMV.jpg"/>

结果发现 消息可以成功发送到消息队列 这就是使用头部信息进行路由

这样 我们就介绍完了所有五种类型的交换机