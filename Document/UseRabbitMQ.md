### 使用消息队列
我们就从最简单的模型开始讲起:

<img src="https://image.itbaima.net/markdown/2023/03/08/GWkUJx1g8ZnTV57.jpg"/>

(一个生产者 -> 消息队列 -> 一个消费者)

生产者只需要将数据丢进消息队列 而消费者只需要将数据从消息队列中取出 这样就实现了生产者和消费者的消息交互 我们现在来演示一下 首先进入到我们的管理页面 这里我们创建一个新的实验环境 只需要新建一个Virtual Host即可:

<img src="https://image.itbaima.net/markdown/2023/03/08/PzehXHuDyFANIKV.jpg"/>

添加新的虚拟主机之后 我们可以看到 当前admin用户的主机访问权限中新增了我们刚刚添加的环境:

<img src="https://image.itbaima.net/markdown/2023/03/08/9cGyunKrTvjfDRp.jpg"/>

现在我们来来看看Exchange(交换机):

<img src="https://image.itbaima.net/markdown/2023/03/08/GDnFoizW86pC5l9.jpg"/>

Exchange列表中自动为我们新增了刚刚创建好的虚拟主机相关的预设交换机 一共7个 这里我们首先介绍一下前面两个direct类型的交换机 一个是(AMQP default)还有一个是amq.direct 它们都是直连模式的交换机 我们来看看第一个:

<img src="https://image.itbaima.net/markdown/2023/03/08/lIpfxGjLPrOatE5.jpg"/>

第一个交换机是所有虚拟主机都会自带的一个默认交换机 并且此交换机不可删除 此交换机默认绑定到所有的消息队列 如果是通过默认交换机发送消息
那么会根据消息的routingkey(之后我们发消息都会指定)决定发送给哪个同名的消息队列 同时也不能显示地将消息队列绑定或解绑到此交换机

我们可以看到 详细信息中 当前交换机特性是持久化的 也就是说就算机器重启 那么此交换机也会保留 如果不是持久化 那么一旦重启就会消失
实际上我们在列表中看到D的字样 就表示此交换机是持久化的 包括一会我们要讲解的消息队列列表也是这样 所有自动生成的交换机都是持久化的

我们接着来看第二个交换机 这个交换机是一个普通的直连交换机:

<img src="https://image.itbaima.net/markdown/2023/03/08/DnpENxIPgOUTSbM.jpg"/>

这个交换机和我们刚刚介绍的默认交换机类型一致 并且也是持久化的 但是我们可以看到它是具有绑定关系的 如果没有指定的消息队列绑定到此交换机上
那么这个交换机无法正常将信息存放到指定的消息队列中 也是根据routingkey寻找消息队列(但是可以自定义)

我们可以在下面直接操作 让某个队列绑定 这里我们先不进行操作

介绍完了两个最基本的交换机之后(其他类型的交换机我们会在后面进行介绍) 我们接着来看消息队列:

<img src="https://image.itbaima.net/markdown/2023/03/08/q7WcUvZp8NhMb9f.jpg"/>

可以看到消息队列列表中没有任何的消息队列 我们可以来尝试添加一个新的消息队列:

<img src="https://image.itbaima.net/markdown/2023/03/08/D8hv6Kbo3eSNzVp.jpg"/>

第一行 我们选择我们刚刚创建好的虚拟主机 在这个虚拟主机下创建此消息队列 接着我们将其类型定义为Classic类型 也就是经典类型(其他类型我们会在后面逐步介绍) 名称随便起一个
然后持久化我们选择Transient暂时的(当然也可以持久化 看你自己) 自动删除我们选择No(需要至少有一个消费者连接到这个队列 之后 一旦所有与这个队列连接的消费者都断开时 就会自动删除此队列)
最下面的参数我们暂时不进行任何设置(之后会用到)

现在 我们就创建好了一个经典的消息队列:

<img src="https://image.itbaima.net/markdown/2023/03/08/yGSt4HbT7iX3Nze.jpg"/>

点击此队列的名称 我们可以查看详细信息:

<img src="https://image.itbaima.net/markdown/2023/03/08/NGCFKhcUf9lOADX.jpg"/>

详细信息中包括队列的当前负载状态, 属性, 消息队列占用的内存, 消息数量等 一会我们发送消息时可以进一步进行观察

现在我们需要将此消息队列绑定到上面的第二个直连交换机 这样我们就可以通过此交换机向此消息队列发送消息了:

<img src="https://image.itbaima.net/markdown/2023/03/08/NGCFKhcUf9lOADX.jpg"/>

这里填写之前第二个交换机的名称还有我们自定义的routingkey(最好还是和消息队列名称一致 这里是为了一会演示两个交换机的区别用) 我们直接点击绑定即可:

<img src="https://image.itbaima.net/markdown/2023/03/08/u95NJG2YskOBpXl.jpg"/>

绑定之后我们可以看到当前队列已经绑定对应的交换机了 现在我们可以前往交换机对此消息队列发送一个消息:

<img src="https://image.itbaima.net/markdown/2023/03/08/MBIDVqzf8oce2L4.jpg"/>

回到交换机之后 可以卡到这边也是同步了当前的绑定信息 在下方 我们直接向此消息队列发送信息:

<img src="https://image.itbaima.net/markdown/2023/03/08/htEoZ49zu6mipCM.jpg"/>

点击发送之后 我们回到刚刚的交换机详细页面 可以看到已经有一条新的消息在队列中了:

<img src="https://image.itbaima.net/markdown/2023/03/08/nO9eUjMRbCmBqPT.jpg"/>

我们可以直接在消息队列这边获取消息队列中的消息 找到下方的Get Message选项:

<img src="https://image.itbaima.net/markdown/2023/03/08/emrY3SZ98CJRAOh.jpg"/>

可以看到有三个选择 首先第一个Ack Mode 这个是应答模式选择 一共有四个选项:

<img src="https://image.itbaima.net/markdown/2023/03/08/nrWPuoGRTp7F36e.jpg"/>

- `Nack message requeue true`: 拒绝消息 也就是说不会将消息从消息队列取出 并且重新排队 一次可以拒绝多个消息
- `Ack message requeue true/false`: 确认应答 确认后消息会从消息队列中移除 一次可以确认多个消息
- `Reject message requeue true/false`: 也是拒绝此消息 但是可以指定是否重新排队

这里我们使用默认的就可以了 这样只会查看消息是啥 但是不会取出 消息依然存在于消息队列中 第二个参数是编码格式 使用默认的就可以了 最后就是要生效的操作数量 选择1就行:

<img src="https://image.itbaima.net/markdown/2023/03/08/c6auDXoHFqZT9M2.jpg"/>

可以看到我们刚刚的消息已经成功读取到

现在我们再去第一个默认交换机中尝试发送消息试试看:

<img src="https://image.itbaima.net/markdown/2023/03/08/t5V3yQ8kbOKRpxf.jpg"/>

如果我们使用之前自定义的柔routingkey 会显示没有路由 这是因为默认的交换机只会找对应名称的消息队列 我们现在向yyds发送一下试试看:

<img src="https://image.itbaima.net/markdown/2023/03/08/LCVPvykIjMox9m1.jpg"/>

可以看到消息成功发布了 我们来接收一下看看:

<img src="https://image.itbaima.net/markdown/2023/03/08/9jsdfADB5HRC7wP.jpg"/>

可以看到成功发送到此消息队列中了

当然除了在交换机发送消息给消息队列之外 我们也可以直接在消息队列这里发:

<img src="https://image.itbaima.net/markdown/2023/03/08/cYPwJnbiZlmvqD3.jpg"/>

效果是一样的 注意这里我们可以选择是否将消息持久化 如果是持久化消息 那么就算服务器重启 此消息也会保存在消息队列中

最后如果我们不需要再使用此消息队列了 我们可以手动对其进行删除或是清空:

<img src="https://image.itbaima.net/markdown/2023/03/08/kJE5xwgZOTGWjLq.jpg"/>

点击Delete Queue删除我们刚刚创建好的yyds队列 到这里 我们对应消息队列的一些简单使用 就讲解完毕了