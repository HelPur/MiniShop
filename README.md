# Mini Shop

一个小型电商平台示例，用来覆盖 Spring Boot MVC、依赖注入、事务、AOP、拦截器、SQL、索引、Redis 数据结构、分布式锁、RocketMQ 生产者/消费者等知识点。

## 运行

```bash
mvn spring-boot:run
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

事务观察点:

- 下单扣库存和生成订单在一个事务中完成。
- 支付回调更新支付记录和订单状态在一个事务中完成。
- 如果库存不足或支付状态不合法，事务会回滚。
