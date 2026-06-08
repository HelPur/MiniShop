# Mini Shop

一个小型电商平台示例，用来覆盖 Spring Boot MVC、依赖注入、事务、AOP、拦截器、SQL、索引、Redis 数据结构、分布式锁、RocketMQ 生产者/消费者等知识点。

## 技术点对应

- Spring MVC: `@RestController` 暴露用户、商品、购物车、订单、支付接口。
- 依赖注入: 所有 Service/Controller 都使用构造器注入。
- 事务: 注册、商品维护、下单、支付回调、发货、收货、退款使用 `@Transactional`。
- AOP: `AuditAspect` 记录 Service 调用，并通过 `@RequireRole` 做管理员权限校验。
- 拦截器: `AuthInterceptor` 从 `X-User-Id` 请求头解析当前用户。
- SQL: H2 内存数据库，启动时执行 `data.sql` 初始化用户、分类、商品。
- 索引: 实体上通过 `@Index` 给用户名、商品分类/状态、订单用户/状态、支付流水号等字段建索引。
- Redis: 默认用内存购物车；启动 `redis` profile 后，购物车使用 Redis Hash: `cart:{userId}`，分布式锁使用 `SETNX + TTL`。
- RocketMQ: 默认打印订单事件；启动 `rocketmq` profile 后，`RocketMqOrderEventPublisher` 生产消息，`OrderPaidConsumer` 消费消息。

## 运行

```bash
mvn spring-boot:run
```

打开 H2 控制台:

```text
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:minishop
username: sa
password: 留空
```

启用 Redis 示例:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=redis
```

启用 RocketMQ 示例:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=rocketmq
```

## 默认账号

- 管理员: `admin` / `admin`
- 普通用户: `alice` / `alice`

登录成功后，后续需要登录的接口带上:

```text
X-User-Id: 2
```

管理员接口带:

```text
X-User-Id: 1
```

## 接口示例

注册:

```http
POST /api/users/register
{
  "username": "bob",
  "password": "bob",
  "email": "bob@example.com",
  "phone": "13700000000"
}
```

登录:

```http
POST /api/users/login
{
  "username": "alice",
  "password": "alice"
}
```

查看商品:

```http
GET /api/products
```

添加购物车:

```http
POST /api/cart
X-User-Id: 2
{
  "productId": 1,
  "quantity": 2
}
```

创建订单:

```http
POST /api/orders
X-User-Id: 2
```

发起支付:

```http
POST /api/payments
X-User-Id: 2
{
  "orderId": 1,
  "method": "ALIPAY"
}
```

模拟支付回调:

```http
POST /api/payments/callback
{
  "tradeNo": "PAY-替换为支付接口返回的流水号",
  "success": true,
  "channelMessage": "mock success"
}
```

管理员发货:

```http
PATCH /api/orders/1/ship
X-User-Id: 1
```

用户收货:

```http
PATCH /api/orders/1/receive
X-User-Id: 2
```

## 推荐 SQL 练习

```sql
select * from products where status = 'ON_SHELF';
select * from orders where user_id = 2 order by created_at desc;
select p.name, sum(i.quantity) sold_count
from order_items i
join products p on p.id = i.product_id
group by p.name;
```

事务观察点:

- 下单扣库存和生成订单在一个事务中完成。
- 支付回调更新支付记录和订单状态在一个事务中完成。
- 如果库存不足或支付状态不合法，事务会回滚。
