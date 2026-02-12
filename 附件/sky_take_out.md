## Nginx服务器的反向代理与负载均衡
优点:
![[Pasted image 20260129133630.png]]

nginx负载均衡策略
![[Pasted image 20260129134600.png]]

## MD5加密技术
```Java
password = DigestUtils.md5DigestAsHex(password.getBytes());
```

## Swagger
### 代码实现(定义在Configuration类中)
1.加入knife4j相关配置
```Java
@Bean  
public Docket docket() {  
  
    ApiInfo apiInfo = new ApiInfoBuilder()  
            .title("苍穹外卖项目接口文档")  
            .version("2.0")  
            .description("苍穹外卖项目接口文档")  
            .build();  
    Docket docket = new Docket(DocumentationType.SWAGGER_2)  
            .apiInfo(apiInfo)  
            .select()  
            .apis(RequestHandlerSelectors.basePackage("com.sky.controller"))// 指定要扫描的包  
            .paths(PathSelectors.any())// 指定要扫描的路径  
            .build();  
  
    return docket;  
}  

```
  2.设置静态资源映射
```Java
/**  
 * 设置静态资源映射  
 * @param registry  
 */  
protected void addResourceHandlers(ResourceHandlerRegistry registry) {  
    log.info("开始进行静态资源映射...");  
    registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");  
    registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
```
### 常用注解
通过注解可以控制生成的接口文档,使接口文档具有更好的可读性
![[Pasted image 20260129144927.png]]

## 对象属性拷贝
拷贝属性名一致的属性值
```Java
//对象属性拷贝  
BeanUtils.copyProperties(employeeDTO, employee);
```

## ThreadLocal
**用于在同一个请求线程内跨方法、跨层级共享数据（如用户信息），无需参数传递，实现线程隔离的上下文管理**
工具类实现
```Java
public class BaseContext {  
  
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();  
  
    public static void setCurrentId(Long id) {  
        threadLocal.set(id);  
    }  
  
    public static Long getCurrentId() {  
        return threadLocal.get();  
    }  
  
    public static void removeCurrentId() {  
        threadLocal.remove();  
    }  
  
}
```

## 扩展消息转换器
![[Pasted image 20260211213122.png]]
时间格式转换的代码实现,写在MVC配置类中,需要继承WebMvcConfigurationSupport类
```Java
@Override  
protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {  
    log.info("扩展消息转换器...");  
    //创建消息转换器对象  
    MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();  
    //为消息转换器设置一个对象转换器，对象转换器可以将java对象转为json  
    messageConverter.setObjectMapper(new JacksonObjectMapper());  
  
    //将上面的消息转换器对象追加到mvc框架的转换器集合中  
    converters.add(0,messageConverter);  
}
```
对象转换器内容固定,直接使用即可

对单个属性格式的转换:
```Java
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")  
private LocalDateTime createTime;  // 记录创建时间
```

## Spring AOP
### 核心概念:

在切面类中实现切面,要在切面类的类名前加上@Aspect注解

- 连接点: JoinPoint 可以被AOP控制的方法

- 通知: Advice 指的是重复的逻辑,也就是共性功能,最终体现为通知方法

- 切入点: PointCut 连接点匹配的条件,通知仅会在切入点方法执行时被应用

- 切面: Aspect 描述通知与切入点的对应关系(通知+切入点)

- 目标对象: Target 通知所应用的对象

### 执行流程(动态代理):
![[Pasted image 20260131132716.png]]
### 通知类型

- @Around: 环绕通知，此注解标注的通知方法在目标方法前、后都被执行
    
- @Before: 前置通知，此注解标注的通知方法在目标方法前被执行
    
- @After: 后置通知，此注解标注的通知方法在目标方法后被执行，无论是否有异常都会执行
    
- @AfterReturning: 返回后通知，此注解标注的通知方法在目标方法后被执行，有异常不会执行
    
- @AfterThrowing: 异常后通知，此注解标注的通知方法发生异常后执行

注意:
@Around环绕通知需要自己调用 ProceedingJoinPoint.proceed() 让原始方法执行,其他通知不需要考虑目标方法执行
@Around环绕通知方法的返回值必须指定为Object类型,用于接收原始方法返回值

### 通知顺序
![[Pasted image 20260131140559.png]]


### 切入点表达式

#### execution匹配方法签名
语法:

execution(<u>访问修饰符</u>==?==  返回值  <u>包名.类名.</u> ==?==  方法名(形参数据类型)  <u>throws  异常</u>==?==)

带 ==?== 的部分可以省略, 包名.类名.不建议省略

切入点的两种通配符用法

**`*`（一个星号）**
**含义**：代表**一个独立的任意符号**。
**应用**：
1. 可以匹配任意的返回值类型、包名、类名或方法名。
2. 可以匹配任意类型的一个参数。
3. 可以通配包、类或方法名的一部分。

**`..`（两个点）**
**含义**：代表**多个连续的任意符号**。
**应用**：
1. 可以匹配任意层级的子包。
2. 可以匹配任意个数、任意类型的参数。



**书写建议**:

- 所有业务方法名在命名时尽量规范，方便切入点表达式快速匹配。如：findXxx，updateXxx。
    
- 描述切入点方法通常基于接口描述，而不是直接描述实现类，增强拓展性。
    
- 在满足业务需要的前提下，尽量缩小切入点的匹配范围。如：包名尽量不使用..，使用*匹配单个包。

#### annotation匹配特定注解
语法:

@annotation(注解全类名)

![[Pasted image 20260131170617.png]]


**注意**:两种表达式之间可以使用逻辑运算符 || , && , ! 来连接
#### @Pointcut注解
将公共的切点表达式抽取出来,需要用到时引用该切点表达式即可

```Java
@Pointcut("execution(* com.itheima.service.impl.DeptServiceImpl.(..))")
public void pt(){};

@Around("pt()")
public Object recordTime(ProceedingJoinPoint joinPoint) throws Throwable {}
```

### 连接点

在Spring中用JoinPoint抽象了连接点，用它可以获得方法执行时的相关信息，如目标类名、方法名、方法参数等。

**注意**:
- 对于@Around通知，获取连接点信息只能使用 ProceedingJoinPoint。
    
- 对于其它四种通知，获取连接点信息只能使用JoinPoint，它是 ProceedingJoinPoint的父类型。

获取参数的方法:

```Java
@Before("@annotation(全类名)")
public void before(JoinPoint joinPoint){
    //1. 获取目标对象
    Object target = joinPoint.getTarget();

    //2. 获取目标类
    String className = joinPoint.getTarget().getClass().getName();

    //3. 获取目标方法
    String methodName = joinPoint.getSignature().getName();

    //4. 获取目标方法参数
    Object[] args = joinPoint.getArgs();

}
```

## 方法签名对象


```Java
MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    
    // 获取方法名
    String methodName = signature.getName();
    
    // 获取返回类型
    Class<?> returnType = signature.getReturnType();
    
    // 获取参数类型
    Class<?>[] paramTypes = signature.getParameterTypes();
    
    // 获取方法对象（可用于获取注解等）
    Method method = signature.getMethod();
    
    // 获取参数值
    Object[] args = joinPoint.getArgs();
    
    // 执行原始方法
    Object result = joinPoint.proceed();
```

## Redis基础操作
### 数据结构特点
![[Pasted image 20260204144926.png]]

### 常用命令

通用命令
```bash
KEYS pattern                # 查找所有符合给定模式( pattern)的 key

EXISTS key                  # 检查给定 key 是否存在

TYPE key                    # 返回 key 所储存的值的类型

DEL key                     # 删除指定的 key
```

字符串操作命令
```bash
SET key value               #设置指定key的值

GET key                     #获取指定key的值

SETEX key seconds value     #设置指定key的值，并将 key 的过期时间设为 seconds 秒

SETNX key value             #只有在 key 不存在时设置 key 的值
```

哈希操作命令
```bash
HSET key field value         # 将哈希表 key 中的字段 field 的值设为 value

HGET key field               # 获取存储在哈希表中指定字段的值

HDEL key field               # 删除存储在哈希表中的指定字段

HKEYS key                    # 获取哈希表中所有字段

HVALS key                    # 获取哈希表中所有值
```

列表操作命令
```bash
LPUSH key value1 [value2]    # 将一个或多个值插入到列表头部

LRANGE key start stop        # 获取列表指定范围内的元素

RPOP key                     # 移除并获取列表最后一个元素

LLEN key                     # 获取列表长度
```
集合操作命令
```bash
SADD key member1 [member2]   # 向集合添加一个或多个成员

SMEMBERS key                 # 返回集合中的所有成员

SCARD key                    # 获取集合的成员数

SINTER key1 [key2]           # 返回给定所有集合的交集

SUNION key1 [key2]           # 返回所有给定集合的并集

SREM key member1 [member2]   # 删除集合中一个或多个成员(set remove)
```
有序集合操作命令
```bash
ZADD key score1 member1 [score2 member2]   # 向有序集合添加一个或多个成员

ZRANGE key start stop [WITHSCORES]         # 通过索引区间返回有序集合中指定区间内的成员

ZINCRBY key increment member               # 有序集合中对指定成员的分数加上增量increment

ZREM key member [member ...]               # 移除有序集合中的一个或多个成员
```

## Spring Data Redis


1.导入Spring Data Redis的maven坐标
```xml
<dependency>  
    <groupId>org.springframework.boot</groupId>  
    <artifactId>spring-boot-starter-data-redis</artifactId>  
</dependency>
```
2.配置Redis数据源
```xml
spring:  
  redis:  
    host: localhost  
    port: 6379  
    database: 0
```
3.编写配置类,创建RedisTemplate对象
```Java
@Slf4j  
@Configuration  
public class RedisConfigration {  
  
    @Bean  
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){  
        log.info("创建Redis模板对象...");  
        RedisTemplate redisTemplate = new RedisTemplate();  
        //设置Redis连接工厂对象  
        redisTemplate.setConnectionFactory(redisConnectionFactory);  
        //设置Redis的key的序列化器  
        redisTemplate.setKeySerializer(new StringRedisSerializer());  
        return redisTemplate;  
    }  
}
```
4.通过RedisTemplate对象操作Redis
```Java
//获取操作每种类型的对象
public void getRedisOperations(){  
  
    ValueOperations valueOperations = redisTemplate.opsForValue();  
    HashOperations hashOperations = redisTemplate.opsForHash();  
    ListOperations listOperations = redisTemplate.opsForList();  
    SetOperations setOperations = redisTemplate.opsForSet();  
    ZSetOperations zSetOperations = redisTemplate.opsForZSet();  
  
}
```

```Java
// 键操作命令
redisTemplate.keys(pattern);                 // KEYS pattern - 查找所有符合模式的 key
redisTemplate.hasKey(key);                   // EXISTS key - 检查 key 是否存在
redisTemplate.type(key);                     // TYPE key - 返回 key 储存值的类型
redisTemplate.delete(key);                   // DEL key - 删除指定的 key
redisTemplate.expire(key, timeout, timeUnit); // EXPIRE key seconds - 设置过期时间
redisTemplate.getExpire(key);                // TTL key - 获取剩余生存时间

// 字符串类型命令
redisTemplate.opsForValue().set(key, value);  // SET key value
redisTemplate.opsForValue().get(key);         // GET key
redisTemplate.opsForValue().set(key, value, timeout, timeUnit); // SETEX key seconds value
redisTemplate.opsForValue().setIfAbsent(key, value); // SETNX key value

// 哈希类型命令
redisTemplate.opsForHash().put(key, field, value);  // HSET key field value
redisTemplate.opsForHash().get(key, field);         // HGET key field
redisTemplate.opsForHash().delete(key, field);      // HDEL key field
redisTemplate.opsForHash().keys(key);               // HKEYS key
redisTemplate.opsForHash().values(key);             // HVALS key

// 列表类型命令
redisTemplate.opsForList().leftPush(key, value);     // LPUSH key value
redisTemplate.opsForList().range(key, start, end);   // LRANGE key start stop
redisTemplate.opsForList().rightPop(key);            // RPOP key
redisTemplate.opsForList().size(key);                // LLEN key

// 集合类型命令
redisTemplate.opsForSet().add(key, members);          // SADD key member
redisTemplate.opsForSet().members(key);               // SMEMBERS key
redisTemplate.opsForSet().size(key);                  // SCARD key
redisTemplate.opsForSet().intersect(key, otherKeys);  // SINTER key1 key2
redisTemplate.opsForSet().union(key, otherKeys);      // SUNION key1 key2
redisTemplate.opsForSet().remove(key, members);       // SREM key member

// 有序集合类型命令
redisTemplate.opsForZSet().add(key, member, score);   // ZADD key score member
redisTemplate.opsForZSet().range(key, start, end);    // ZRANGE key start stop
redisTemplate.opsForZSet().incrementScore(key, member, delta); // ZINCRBY key increment member
redisTemplate.opsForZSet().remove(key, members);      // ZREM key member

```

## HttpClient
用于提供高效的,最新的,功能丰富的支持HTTP协议的客户端编程工具包,并且它支持HTTP协议最新的版本和建议

发送请求步骤:
Get请求
```Java
public void testGet() throws IOException {

	// 创建HttpClient对象  
	CloseableHttpClient httpClient = HttpClients.createDefault();  

	// 创建Http请求对象 
	HttpGet httpGet = new HttpGet("http://localhost:8080/user/shop/status");  

	// 调用HttpClient的execute方法发送请求 
	CloseableHttpResponse response = httpClient.execute(httpGet);

	//关闭资源  
	response.close();  
	httpClient.close();
}
```

Post请求
```Java
public void testPost() throws IOException, JSONException {  

    // 创建httpClient对象  
    CloseableHttpClient httpClient = HttpClients.createDefault();  
  
    // 创建请求对象  
    HttpPost httpPost = new HttpPost("http://localhost:8080/admin/employee/login");  
  
    //  处理请求体中数据  
    JSONObject json = new JSONObject();  
    json.put("username", "admin");  
    json.put("password", "123456");  
    StringEntity entity = new StringEntity(json.toString(),"utf-8");  
    
    //   设置请求编码方式与数据格式  
    entity.setContentEncoding("utf-8");  
    entity.setContentType("application/json");  
  
    //   发送请求  
    httpPost.setEntity(entity);  
    CloseableHttpResponse response = httpClient.execute(httpPost);  
  
    // 关闭资源  
    response.close();  
    httpClient.close();  
  
}
```

## Spring Cache

常用注解
![[Pasted image 20260207152653.png]]

## CDATA包装
CDATA的语法是：<![CDATA[ 内容 ]]>，其中的内容会被XML解析器当作纯文本处理，不会解析其中的特殊字符。

## WebSocket

![[Pasted image 20260210173933.png]]

## 业务层避免在循环中调用mapper的方案
### 业务层通过stream流的数据转换（day9历史订单查询接口）
代码

```java
@Transactional  
@Override  
public PageResult searchHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO) {  
    PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());  
    Page<OrderVO> page = orderMapper.historyOrdersQuery(ordersPageQueryDTO);  
  
    List<OrderVO> orders = page.getResult();  
  
    // 优化  
    if (!orders.isEmpty()) {  
        // 批量获取所有订单ID  
        List<Long> orderIds = orders.stream()  
                .map(OrderVO::getId)  
                .collect(Collectors.toList());  
  
        // 一次性查询所有订单详情  
        Map<Long, List<OrderDetail>> detailMap = orderDetilMapper.listByOrderIds(orderIds)  
                .stream()  
                .collect(Collectors.groupingBy(OrderDetail::getOrderId));  
  
        // 批量设置订单详情  
        for (OrderVO order : orders) {  
            order.setOrderDetailList(detailMap.getOrDefault(order.getId(), new ArrayList<>()));  
        }  
    }  
  
    return new PageResult(page.getTotal(), orders);  
}
```

collect() 和 Collectors 的区别

1. collect() 方法
位置：是Stream接口的终端操作方法
作用：将Stream流中的元素收集到某种容器中
语法：stream.collect(collector)
2. Collectors 类
性质：是一个工具类（java.util.stream.Collectors）
作用：提供各种预定义的收集器（Collector）
包含：toList()、toSet()、toMap()、groupingBy()等静态方法

Collectors.groupingBy() 是一个收集器，它的作用是：

分组依据：OrderDetail::getOrderId - 以每个OrderDetail对象的orderId属性作为分组的key
返回结果：Map<Long, List<OrderDetail>> - key是orderId，value是具有相同orderId的OrderDetail对象列表



