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





















