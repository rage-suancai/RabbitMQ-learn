<img src="https://image.itbaima.net/markdown/2023/03/08/9a2q4ZBuWxJs861.jpg"/>

### 消息队列
经过前面的学习 我们已经了解了我们之前的技术在分布式环境下的应用 接着我们来看最后一章的内容

那么 什么是消息队列呢?

我们之前如果需要进行远程调用 那么一般可以通过发送HTTP请求来完成 而现在 我们可以使用第二种方式 就是消息队列
它能够将发送方发送的信息放入队列中 当新的消息入队时 会通知接收方进行处理 一般消息发送方称为生产者 接收方称为消费者

<img src="https://image.itbaima.net/markdown/2023/03/08/yknBVt2jGgFSTO8.jpg"/>

这样我们所有的请求 都可以直接丢到消息队列中 再由消费者取出 不再是直接连接消费者的形式了 而是加了一个中间商 这也是一种很好的解耦方案 并且在高并发的情况下
由于消费者能力有限 消息队列也能起到一个削峰填谷的作用 堆积一部分的请求 再由消费者来慢慢处理 而不会像直接调用那样请求蜂拥而至

那么 消息队列具体实现有哪些呢:
- RabbitMQ - 性能很强 吞吐量很高 支持多种协议 集群化 消息的可靠执行特性等优势 很适合企业的开发
- Kafka - 提供了超高的吞吐量 ms级别的延迟 极高的可用性以及可靠性 而且分布式可以任意扩展
- RocketMQ - 阿里巴巴推出的消息队列 经历过双十一的考验 单机吞吐量高 消息的高可靠性 扩展性强 支持事务等 但是功能不够完整 语言支持性较差

我们这里 主要讲解的是RabbitMQ消息队列

### RabbitMQ消息队列
官方网站: https://www.rabbitmq.com

    RabbitMQ拥有数万计的用户 是最受欢迎的开源消息队列之一 从T-Mobile到Runtastic RabbitMQ在全球范围内用于小型初创企业和大型企业
    RabbitMQ轻量级 易于在本地和云端部署 它支持多种消息协议 RabbitMQ可以部署在分布式和联合配置中 以满足大规模 高可用性要求
    RabbitMQ在许多操作系统和云环境中运行 并为大多数流行语言提供了广泛的开发者工具

我们首先还是来看看如何进行安装

### 安装消息队列
下载地址: https://www.rabbitmq.com/download.html

由于除了消息队列本身之外还需要Erlang环境(RabbitMQ就是这个语言开发的) 所以我们就在我们的Ubuntu服务器上进行安装

首先是Erlang 比较大 1GB左右:

```shell
                    sudo apt install erlang
```

接着安装RabbitMQ:

```shell
                    sudo apt install rabbitmq-server
```

安装完成后 可以输入:

```shell
                    sudo rabbitmqctl status
```

来查看当前的RabbitMQ运行状态 包括运行环境, 内存占用, 日志文件等信息:

                        Runtime
    
                        OS PID: 13718
                        OS: Linux
                        Uptime (seconds): 65
                        Is under maintenance?: false
                        RabbitMQ version: 3.8.9
                        Node name: rabbit@ubuntu-server-2
                        Erlang configuration: Erlang/OTP 23 [erts-11.1.8] [source] [64-bit] [smp:2:2] [ds:2:2:10] [async-threads:64]
                        Erlang processes: 280 used, 1048576 limit
                        Scheduler run queue: 1
                        Cluster heartbeat timeout (net_ticktime): 60

这样我们的RabbitMQ服务器就安装完成了 要省事还得是Ubuntu啊

可以看到默认有两个端口被使用:

                        Listeners

                        Interface: [::], port: 25672, protocol: clustering, purpose: inter-node and CLI tool communication
                        Interface: [::], port: 5672, protocol: amqp, purpose: AMQP 0-9-1 and AMQP 1.0

我们一会主要使用的就是amqp协议的那个端口5672来进行连接 25672是集群化端口 之后我们也会用到

接着我们还可以将RabbitMQ的管理面板开启 这样的话就可以在浏览器上进行实时访问和监控了:

```shell
                    sudo rabbitmq-plugins enable rabbitmq_management
```

                        Listeners

                        Interface: [::], port: 25672, protocol: clustering, purpose: inter-node and CLI tool communication
                        Interface: [::], port: 5672, protocol: amqp, purpose: AMQP 0-9-1 and AMQP 1.0
                        Interface: [::], port: 15672, protocol: http, purpose: HTTP API

我们打开浏览器直接访问一下:

<img src="https://image.itbaima.net/markdown/2023/03/08/HxtXlqi7BUYWdC2.jpg"/>

可以看到需要我们进行登录才可以进入 我们这里还需要创建一个用户才可以 这里就都用admin:

```shell
                    sudo rabbitmqctl add_user 用户名 密码
```

将管理员权限给与我们刚刚创建好的用户:

```shell
                    sudo rabbitmqctl set_user_tags admin administrator
```

创建完成之后 我们登录一下页面:

<img src="https://image.itbaima.net/markdown/2023/03/08/eEJMsxhc5Onpld8.jpg"/>

进入之后会显示当前的消息队列情况 包括版本号 Erlang版本等 这里需要介绍一下RabbitMQ的设计架构 这样我们就知道各个模块管理的是什么内容了:

<img src="https://image.itbaima.net/markdown/2023/03/08/j5kIgD9ZRQiGtd6.jpg"/>

- `生产者(Publisher)和消费者(Consumer)`: 不用多说了吧
- `Channel`: 我们的客户端连接都会使用一个Channel 再通过Channel去访问到RabbitMQ服务器 注意通信协议不是http 而是amqp协议
- `Exchange`: 类似于交换机一样的存在 会根据我们的请求 转发给相应的消息队列 每个队列都可以绑定到Exchange上 这样Exchange就可以将数据转发给队列了 可以存在很多个 不同的Exchange类型可以用于实现不同消息的模式
- `Queue`: 消息队列本体 生产者所有的消息都存放在消息队列中 等待消费者取出
- `Virtual Host`: 有点类似于环境隔离 不同环境都可以单独配置一个Virtual Host 每个Virtual Host可以包含很多个Exchange和Queue 每个Virtual Host相互之间不影响