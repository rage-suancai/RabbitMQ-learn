### 死信队列
消息队列中的数据 如果迟迟没有消费者来处理 那么就会一直占用消息队列的空间 比如我们模拟一下抢车票的场景 用户下单高铁票之后 会进行抢座 然后再进行付款
但是如果用户下单在后并没有及时的付款 这张票不可能一直让这个用户占用着 因为你不买别人还要买呢 所以会在一段时间后超时 让这张票可以继续被其他人购买

这时 我们就可以使用死信队列 将那些用户超时未付款的或是用户主动取消的订单 进行进一步的处理 以下类型的消息都会被判定为死信:
- 消息被拒绝(basic.reject/basic.nack)
- 消息TTL过期
- 队列达到最大长度

<img src="https://image.itbaima.net/markdown/2023/03/08/itUWySuA9kvcEgs.jpg"/>

那么如何构建这样的一种使用模式呢? 实际上本质就是一个死信交换机+绑定的死信队列 当正常队列中的消息被绑定为死信时 会被发送到对应的死信交换机 然后再通过交换机发送到死信队列中 死信队列也有对应的消费者去处理消息

这里我们直接再配置类中创建一个新的死信交换机和死信队列 并进行绑定:

```java
                    @Configuration
                    public class RabbitConfiguration {
                    
                        @Bean("directDlExchange")
                        public Exchange exchange() {
                            // 创建一个新的死信交换机
                            return ExchangeBuilder.directExchange("dlx.direct").build();
                        }
                    
                        @Bean("yydsDlQueue") // 创建一个新的死信队列
                        public Queue dlqueue() {
                    
                            return QueueBuilder
                                    .nonDurable("dl-yyds")
                                    .build();
                    
                        }
                    
                        @Bean("dlBinding") // 死信交换机和死信队列进行绑定
                        public Binding dlBinding(@Qualifier("directDlExchange") Exchange exchange,
                                                 @Qualifier("yydsDlQueue") Queue queue) {
                    
                            return BindingBuilder
                                    .bind(queue)
                                    .to(exchange)
                                    .with("dl-yyds")
                                    .noargs();
                    
                        }
                                
                                    ...
                    
                        @Bean("yydsQueue")
                        public Queue queue() {
                    
                            return QueueBuilder
                                    .nonDurable("yyds")
                                    .deadLetterExchange("dlx.direct") // 指定死信交换机
                                    .deadLetterRoutingKey("dl-yyds") // 指定死信Routingkey
                                    .build();
                    
                        }
                        
                            ...
                    
                    }
```

接着我们将监听器修改为死信队列监听:

```java
                    @Component
                    public class TestListener {
    
                        @RabbitListener(queues = "dl-yyds", messageConverter = "jacksonConverter")
                        public void receiver(User user){
                            System.out.println(user);
                        }
                        
                    }
```

配置完成后 我们来尝试一下启动一下吧 注意启动之前记得把之前的队列给删了 这里要重新定义

<img src="https://image.itbaima.net/markdown/2023/03/08/AdrS9yxnRojfWgL.jpg"/>

队列列表中已经出现了我们刚刚定义好的死信队列 并且yyds队列也支持死信队列发送功能了 现在我们尝试向此队列发送一条消息 但是我们将其拒绝:

<img src="https://image.itbaima.net/markdown/2023/03/08/mLokEWYcQ4PXnar.jpg"/>

可以看到拒绝后 如果不让消息重新排队 那么就会变成死信 可以看到再拒绝后:

<img src="https://image.itbaima.net/markdown/2023/03/08/rgiWVJMbpKzQX46.jpg"/>

现在我们来看看第二种情况 RabbitMQ支持将超过一定时间没被消费的消息自动删除这需要消息队列设定TTL值 如果消息的存活时间超过了Time To Live值 就会被自动删除 自动删除后的消息如果有死信队列 那么就会进入到死信队列中

现在我们将yyds消息队列设定TTL值(毫秒为单位):

```java
                    @Bean("yydsQueue")
                    public Queue queue(){
                        return QueueBuilder
                                .nonDurable("yyds")
                                .deadLetterExchange("dlx.direct")
                                .deadLetterRoutingKey("dl-yyds")
                                .ttl(5000) // 如果5秒没处理 就自动删除
                                .build();
                    }
```

现在我们重启测试一下 注意修改了之后记得删除之前的yyds队列:

<img src="https://image.itbaima.net/markdown/2023/03/08/u8xboyv3aTJ9ZE6.jpg"/>

可以看到现在yyds队列已经具有TTL特性了 我们现在来插入一条新的消息:

<img src="https://image.itbaima.net/markdown/2023/03/08/2qensPxuf3zLoQ1.jpg"/>

可以看到消息5秒钟之后就不见了 而是被丢进了死信队列中

最后我们来看一下当消息队列长度达到最大的情况 现在我们将消息队列的长度进行限制:

```java
                    @Bean("yydsQueue")
                    public Queue queue(){
                        return QueueBuilder
                                .nonDurable("yyds")
                                .deadLetterExchange("dlx.direct")
                                .deadLetterRoutingKey("dl-yyds")
                                .maxLength(3) // 将最大长度设定为3
                                .build();
                    }
```

现在我们重启一下 然后尝试连续插入4条消息:

<img src="https://image.itbaima.net/markdown/2023/03/08/56TsMf24QlhZCYL.jpg"/>

可以看到yyds消息队列新增了Limit特性 也就是限定长度:

```java
                    @Test
                    public void publisher() {
                        for (int i = 0; i < 4; i++) 
                            template.convertAndSend("amq.direct", "my-yyds", new User());
                    }       
```

<img src="https://image.itbaima.net/markdown/2023/03/08/d3lEHLPR4VNF92T.jpg"/>

可以看到因为长度限制为3 所以有一条消息直接被丢进了死信队列中 为了能够更直观地观察消息队列的机制 我们为User类新增一个时间字段:

```java
                    @Data
                    public class User {
    
                        int id;
                        String name;
                        String date = new Data().toString();
                        
                    }   
```

接着每隔一秒钟插入一条:

```java
                    @Test
                    public void publisher() throws InterruptedException {
    
                        for (int i = 0; i < 4; i++) {
                            
                            Thread.sleep(1000);
                            template.convertAndSend("amq.direct", "my-yyds", new User());
                            
                        }
                        
                    }
```

再次进行上述实验 可以发现如果到达队列长度限制 那么每次插入都会把位于队首的消息丢进死信队列 来腾出空间给新来的消息