# 02. Getting Help

> 官方版本：Spring Authorization Server 1.5.8  
> 官方页面：https://docs.spring.io/spring-authorization-server/reference/getting-help.html  
> 本页定位：学习资源、社区支持、问题排查入口。

## 1. 本页主要在教什么

这一页不是讲 OAuth2/OIDC 协议细节，也没有 Java 代码。它主要告诉你：遇到 Spring Authorization Server 问题时，应该优先从哪些官方资源找答案。

核心目标有三个：

1. 认识 Spring Authorization Server 背后的社区来源：它是 Spring Security 团队主导的开源项目。
2. 建立正确的问题排查顺序：先看 How-to、Spring Security 基础、当前参考文档、官方 sample，再去 Stack Overflow 或 GitHub。
3. 知道哪些问题适合问社区，哪些问题适合提 GitHub issue。

对你的 conference 项目来说，这一页的价值不是“学一个配置”，而是建立后续踩坑时的排查路线。因为 SAS 很多问题看起来是 OAuth2 问题，实际可能是 Spring Security 过滤器链、AuthenticationProvider、client 注册配置、JWK、issuer 或 redirect_uri 的问题。

## 2. 它属于 Authorization Server 的哪一部分

| 分类 | 说明 |
|---|---|
| 文档类型 | Getting Help / 获取帮助 |
| 所属部分 | 学习资源与社区支持 |
| 是否涉及启动配置 | 否 |
| 是否涉及客户端注册 | 否 |
| 是否涉及 Token 签发 | 否 |
| 是否涉及 OIDC | 否 |
| 是否涉及 JWK | 否 |
| 是否涉及端点 | 否 |
| 是否涉及数据库持久化 | 否 |
| 是否涉及扩展点 | 否 |

这页不是实现页，而是“遇到问题时怎么查”的指导页。

不过它有一个非常重要的隐含信息：Spring Authorization Server 是建立在 Spring Security 之上的。如果 Spring Security 的基础不扎实，后面很多 SAS 配置会看不懂。

## 3. 按小节逐段解释

## 3.1 Community

官方说明：Spring Authorization Server 是由 Spring Security 团队主导的开源项目。如果你需要帮助，可以通过 Spring Security 社区获取支持。

这句话说明两个点。

### 3.1.1 SAS 不是孤立项目

SAS 虽然是一个单独项目，但它不是脱离 Spring Security 的“独立鉴权产品”。它的核心机制来自 Spring Security：

- `SecurityFilterChain`
- `Authentication`
- `AuthenticationProvider`
- `AuthenticationManager`
- `AuthenticationConverter`
- `OAuth2TokenCustomizer`
- Resource Server JWT 校验能力
- OAuth2 Client 获取 client_credentials Token 的能力

所以学习 SAS 时不能只看 `spring-authorization-server` 包。你需要同时理解 Spring Security 的认证流程、过滤器链顺序、异常处理和授权模型。

### 3.1.2 社区问题要尽量按 Spring Security 方式描述

以后你排查问题时，不要只说“OAuth2 不生效”或者“Token 失败”。应该尽量描述清楚：

- 请求的是哪个端点：`/oauth2/authorize`、`/oauth2/token`、`/oauth2/jwks`；
- 使用什么 grant type：authorization_code、refresh_token、client_credentials；
- client 是 public 还是 confidential；
- client authentication method 是什么；
- 是否启用 PKCE；
- redirect_uri 是什么；
- issuer 配置是什么；
- 报错是 Authorization Server 端、Gateway 端还是 Resource Server 端。

这会极大提高排查效率。

## 3.2 Resources

官方列出了一组推荐资源，下面逐个拆解。

## 3.2.1 How-to Guides

官方建议先尝试 How-to Guides，因为它们提供常见问题的解决方案。

这对你的项目非常重要。Reference 文档解释“组件是什么”，How-to 文档解释“某个真实问题怎么落地”。

结合 conference 项目，优先级应该是：

| How-to | 为什么优先看 |
|---|---|
| SPA + PKCE | 你的浏览器前端要用 Authorization Code + PKCE |
| JPA core services | client、authorization、consent 需要入库 |
| Add authorities as custom claims | system 模块 RBAC 要进入 JWT 或权限上下文 |
| Multitenancy | conference 项目很可能有 tenant_id、organizer_id、conference_id 等上下文 |
| Redis core services | 可用于理解 Redis 存储或缓存授权数据的取舍 |
| OIDC UserInfo | 如果前端或第三方要按 OIDC 标准获取用户信息 |
| Dynamic Client Registration | 如果运营后台要动态注册 client，可以参考，但不一定直接开放标准端点 |

我的建议：后面不是简单按官方 How-to 顺序读，而是按你的项目价值排序读。

## 3.2.2 Spring Security 基础

官方明确建议：学习 SAS 之前，要理解 Spring Security 基础。

这是本页最重要的一句话。

很多 SAS 问题其实不是 SAS 本身的问题，而是 Spring Security 基础问题：

| 表面现象 | 真实可能原因 |
|---|---|
| `/oauth2/authorize` 进不去 | SecurityFilterChain 匹配顺序不对 |
| 登录成功后又跳回登录页 | session、formLogin、认证状态没有正确保存 |
| `/oauth2/token` 返回 401 | client 认证失败，不是用户认证失败 |
| JWT 校验失败 | Resource Server 的 issuer-uri / jwk-set-uri 配错 |
| 自定义登录没生效 | AuthenticationProvider 没加入正确的 AuthenticationManager |
| OIDC UserInfo 没返回自定义字段 | UserInfo mapper 或 claims customizer 配置不对 |

对你来说，至少要掌握这些 Spring Security 知识：

- 多个 `SecurityFilterChain` 的匹配和顺序；
- `requestMatcher` 和 `securityMatcher` 的区别；
- 表单登录和认证状态保存；
- `UserDetailsService`、`AuthenticationProvider`、`AuthenticationManager`；
- Resource Server 的 JWT 校验流程；
- OAuth2 Client 获取 client_credentials Token 的方式；
- `GrantedAuthority` 和 `SCOPE_` 前缀的关系。

## 3.2.3 Read through this documentation

官方建议完整阅读当前参考文档。

这说明 SAS 不适合只复制某个博客代码就上线。尤其你的项目要做生产级鉴权中心，必须理解这些主线内容：

| 文档章节 | 必须掌握的原因 |
|---|---|
| Configuration Model | 知道授权服务器过滤器链、端点、安全配置怎么工作 |
| Core Model / Components | 知道 client、authorization、consent、token 如何持久化和扩展 |
| Protocol Endpoints | 知道每个 OAuth2/OIDC 端点的职责和扩展方式 |
| How-to Guides | 解决 PKCE、JPA、多租户、claims 等真实落地问题 |

如果跳过 Reference，只看 How-to，你会知道“怎么抄”，但不知道“为什么这么写”。

## 3.2.4 Sample Applications

官方建议尝试 sample applications。

样例项目的价值是：

- 快速验证官方推荐配置；
- 对照官方最小实现；
- 看清楚授权服务器、客户端、资源服务器之间的边界；
- 排查自己项目配置时做对照。

但是要注意：sample 不是生产架构。

常见 sample 写法可能包括：

- 内存 client；
- 启动时生成测试密钥；
- 简化的用户账号；
- 简化的 redirect_uri；
- 简化的 Token 生命周期；
- 没有审计；
- 没有多租户；
- 没有复杂 RBAC；
- 没有 Gateway 和 Feign 服务间调用。

所以 sample 的正确使用方式是：

```text
先跑通官方样例
    ↓
理解每个 Bean 为什么存在
    ↓
替换成 conference 项目的持久化和业务实现
    ↓
做安全加固和生产配置
```

不要直接把 sample 复制到生产项目里。

## 3.2.5 Stack Overflow tag

官方建议使用 `spring-authorization-server` tag 在 Stack Overflow 提问。

适合问 Stack Overflow 的问题：

- 某个配置为什么不生效；
- 某个端点为什么返回 401 / 403 / invalid_grant；
- PKCE、redirect_uri、scope、issuer 配置问题；
- 如何自定义某个 Converter、Provider、Customizer；
- Spring Security 和 SAS 配合问题。

不适合直接问 Stack Overflow 的问题：

- “帮我设计完整鉴权中心”；
- “帮我改公司项目代码”；
- “为什么我线上所有服务都不能登录”，但没有任何日志和配置；
- 明显是业务系统 RBAC 建模问题，而不是 SAS 问题。

提问时建议准备这些信息：

```text
Spring Boot version:
Spring Security version:
Spring Authorization Server version:
Grant type:
Client authentication method:
Client type: public / confidential
Authorization Server issuer:
Resource Server issuer-uri:
Request endpoint:
Error response:
Relevant SecurityFilterChain config:
RegisteredClient config:
```

## 3.2.6 GitHub issues

官方建议在 GitHub 上报告 bug 和 enhancement requests。

这说明：GitHub issue 更适合“项目本身的问题”，不是一般使用问题。

适合提 GitHub issue 的情况：

- 你能用官方 sample 复现问题；
- 怀疑是 SAS 本身 bug；
- 文档和实际行为不一致；
- 某个规范支持存在问题；
- 希望增强某个功能；
- 文档中有错误。

不适合提 GitHub issue 的情况：

- 自己项目配置错误；
- 不理解 OAuth2 流程；
- Spring Security 基础用法问题；
- 数据库表设计问题；
- 业务权限模型设计问题。

## 4. 本页涉及的类、接口、Bean、方法

严格来说，这一页没有 Java 代码，也没有列出具体类或 Bean。

但它间接提醒你要补 Spring Security 基础，因此后面你要重点关注这些类：

| 类 / 接口 | 它是什么 | 解决什么问题 | 在 OAuth2/OIDC 流程中的位置 | 什么时候用 | 生产注意 |
|---|---|---|---|---|---|
| `SecurityFilterChain` | Spring Security 过滤器链配置 | 决定哪些请求走哪套安全规则 | 授权端点、登录页、资源接口入口 | auth、gateway、resource service 都会用 | 多条链顺序一定要清楚 |
| `AuthenticationProvider` | 认证逻辑处理器 | 处理一种认证方式 | 用户登录、client 认证、grant 处理 | 自定义登录、短信登录、账号密码登录 | 不要绕开 Spring Security 认证上下文 |
| `AuthenticationConverter` | 请求到认证对象的转换器 | 从 HTTP 请求提取认证参数 | token endpoint、自定义 grant | 自定义认证协议时 | 参数校验要严格 |
| `AuthenticationManager` | 认证调度器 | 调用合适的 Provider | 所有认证流程 | 多 Provider 时 | 异常处理和顺序要清楚 |
| `RegisteredClientRepository` | client 查询接口 | 根据 client_id 找客户端配置 | client 认证、授权、token 签发 | 生产入库时必须自定义或用 JDBC/JPA 实现 | client_secret 不要明文 |
| `OAuth2AuthorizationService` | 授权与 Token 存储服务 | 保存 authorization、code、token | 授权码、刷新、撤销 | 生产持久化 | 要考虑清理过期数据 |
| `OAuth2TokenCustomizer` | Token 自定义器 | 增加 JWT claims | Token 签发阶段 | 写入 user_id、tenant_id、authorities | 不要塞完整权限树 |

## 5. 代码重点拆解

本页没有代码，所以没有具体代码拆解。

但是从学习方式上，你后面看官方 sample 时要按下面思路拆：

```java
@Bean
SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) { ... }
```

重点看：

- 这条过滤器链匹配哪些端点；
- 它和普通登录链的顺序；
- 是否启用 OIDC；
- 异常时跳转还是返回 JSON；
- token endpoint 如何认证 client。

```java
@Bean
RegisteredClientRepository registeredClientRepository() { ... }
```

重点看：

- client_id 是什么；
- public 还是 confidential；
- grant type 有哪些；
- redirect_uri 是否精确；
- 是否 require PKCE；
- access_token / refresh_token 生命周期。

```java
@Bean
JWKSource<SecurityContext> jwkSource() { ... }
```

重点看：

- 是否启动时随机生成；
- 是否有固定 kid；
- 私钥如何保存；
- 旧 key 如何保留；
- Gateway 如何获取公钥。

## 6. 和我的 conference 项目结合

这一页对应到 conference 项目，不是加功能，而是建立“官方资源使用规范”。

我建议你在项目中也维护一个排查文档，例如：

```text
conference-module-auth/docs/troubleshooting.md
```

里面记录：

- 本项目使用的 Spring Boot / Spring Security / SAS 版本；
- auth 模块 issuer；
- 各环境 redirect_uri；
- public client 列表；
- confidential client 列表；
- Gateway resource server 配置；
- Feign client_credentials 配置；
- 常见错误码处理方式；
- 官方文档和 sample 链接。

以后团队成员遇到问题，先查项目排查文档，再查官方文档和样例。

## 7. 放到我的 conference 项目里应该怎么设计

## 7.1 建立 auth 模块排查清单

建议在 `conference-module-auth` 加一个内部文档：

```text
conference-module-auth/
└── docs/
    ├── oauth2-client-registry.md
    ├── token-claims.md
    ├── jwk-rotation.md
    ├── gateway-resource-server.md
    ├── feign-client-credentials.md
    └── troubleshooting.md
```

每个文档负责一个关键主题。

## 7.2 建立版本基线

在 README 或配置文档中明确：

| 组件 | 版本 |
|---|---|
| Spring Boot | 项目实际版本 |
| Spring Security | 由 Boot 管理或显式声明 |
| Spring Authorization Server | 项目实际版本 |
| Spring Cloud Alibaba | 项目实际版本 |
| JDK | Java 21 或项目实际版本 |

为什么要写版本？

因为 SAS 仍在持续演进，不同版本的配置方式、默认端点、类名、弃用 API 可能有差异。排查问题时版本非常重要。

## 7.3 建立问题分类规则

团队内部可以按下面方式判断问题归属：

| 问题类型 | 先查哪里 |
|---|---|
| OAuth2 标准流程不理解 | 官方 Reference |
| PKCE / SPA 登录失败 | 官方 PKCE How-to |
| client 入库问题 | JPA core services How-to |
| JWT claims 问题 | authorities claims How-to |
| 多租户问题 | Multitenancy How-to |
| 过滤器链问题 | Spring Security Reference |
| 官方行为疑似 bug | GitHub issue |
| 具体配置报错 | Stack Overflow / 官方 sample 对照 |

## 7.4 建立最小复现工程

生产项目复杂，遇到 SAS 问题时，不要直接在大项目里猜。

建议保留一个最小复现模块：

```text
examples/conference-module-auth-minimal
```

里面只保留：

- 最小 Authorization Server；
- 一个 public PKCE client；
- 一个 confidential client；
- 一个 Gateway Resource Server 示例；
- 一个 Feign client_credentials 示例。

以后遇到问题，可以先判断是官方机制问题，还是 conference 项目集成问题。

## 8. 容易踩坑的地方

| 坑 | 现象 | 原因 | 解决方式 |
|---|---|---|---|
| 只看博客不看官方文档 | 配置能跑但不知道为什么 | SAS 和 Spring Security 版本差异很大 | 以当前版本官方文档为基准 |
| 把 sample 当生产模板 | 上线后 client、JWK、Token 管理混乱 | sample 为了教学简化很多安全配置 | sample 只用于理解流程，生产要替换实现 |
| 忽略 Spring Security 基础 | 过滤器链、登录状态、认证异常处理全混乱 | SAS 构建在 Spring Security 之上 | 先补 SecurityFilterChain、AuthenticationProvider、Resource Server |
| 提 issue 前没有最小复现 | GitHub issue 无法被维护者处理 | 问题可能来自项目复杂配置 | 用官方 sample 或最小工程复现 |
| 提问不带版本 | 社区无法判断问题 | SAS 不同版本行为可能不同 | 提供 Boot/Security/SAS 版本 |
| 混淆用户认证和 client 认证 | `/oauth2/token` 401 时以为用户密码错 | token endpoint 先认证 client | 区分 user authentication 和 client authentication |
| 不区分 auth、gateway、resource service 错误 | 排查方向错误 | OAuth2 流程跨多个系统 | 根据错误发生位置分层排查 |
| 不保留请求参数 | `invalid_grant`、`invalid_redirect_uri` 难查 | code、redirect_uri、code_verifier 等参数缺失 | 开发环境记录脱敏后的关键参数 |

## 9. 生产级建议

### 9.1 项目内维护官方资源索引

建议在 `conference-module-auth` 的 README 里加入：

- 当前 SAS 版本对应的官方文档链接；
- 官方 samples 链接；
- Spring Security Reference 链接；
- 本项目 OAuth2 客户端配置说明；
- 本项目 Token claims 说明；
- 常见错误码说明。

这样团队成员不会各自搜索不同版本的文章。

### 9.2 问题排查顺序标准化

建议排查顺序：

```text
1. 看错误发生在哪个系统
   - auth
   - gateway
   - resource service
   - frontend
   - feign caller

2. 看错误发生在哪个端点
   - /oauth2/authorize
   - /oauth2/token
   - /oauth2/jwks
   - /.well-known/openid-configuration
   - /userinfo

3. 看是哪种身份
   - 用户身份
   - client 身份
   - 服务身份

4. 看是哪种 grant type
   - authorization_code
   - refresh_token
   - client_credentials

5. 对照 RegisteredClient 配置
   - redirect_uri
   - scope
   - grant type
   - client authentication method
   - require PKCE

6. 对照 Resource Server 配置
   - issuer-uri
   - jwk-set-uri
   - audience
   - authority mapping
```

### 9.3 日志和审计要提前设计

为了以后能排查问题，auth 模块应该记录这些事件：

| 事件 | 是否记录 | 注意事项 |
|---|---:|---|
| 登录成功 | 是 | 记录 user_id、tenant_id、client_id、ip |
| 登录失败 | 是 | 不记录密码，限制错误日志敏感信息 |
| 授权码签发 | 是 | 记录 client_id、redirect_uri、scope |
| Token 签发 | 是 | 记录 grant_type、client_id、subject，不记录 Token 原文 |
| Token 刷新 | 是 | 记录 refresh 行为和 client_id |
| Token 撤销 | 是 | 记录撤销来源 |
| client 认证失败 | 是 | 记录 client_id 和原因，但不记录 secret |
| JWK 轮换 | 是 | 记录 kid、启用时间、停用时间 |

### 9.4 不要泄露敏感信息

排查时要注意：

- 日志不要打印 access_token；
- 日志不要打印 refresh_token；
- 日志不要打印 client_secret；
- GitHub issue / Stack Overflow 不要贴真实 Token；
- 贴 JWT 时要脱敏，或只贴 header / claims 的安全字段；
- redirect_uri 可以贴测试环境，不要贴内部敏感域名。

## 10. 本页总结表

| 官方资源 | 它是什么 | 解决什么问题 | 什么时候用 | conference 项目建议 |
|---|---|---|---|---|
| Spring Security Community | Spring Security 相关社区入口 | 获取社区帮助 | 遇到使用问题时 | 按 Spring Security/SAS 机制描述问题 |
| How-to Guides | 官方专题指南 | 解决常见真实场景 | PKCE、JPA、多租户、claims 等 | 按项目优先级阅读 |
| Spring Security Reference | Spring Security 基础文档 | 理解过滤器链、认证、授权 | 看不懂 SAS 配置时 | 必须补基础 |
| SAS Reference | 当前参考文档 | 理解整体模型和组件 | 系统学习时 | 作为主线文档 |
| Sample Applications | 官方样例项目 | 对照最小可运行配置 | 跑流程、复现问题时 | 不直接复制到生产 |
| Stack Overflow tag | 社区问答 | 具体配置和使用问题 | 有明确报错和上下文时 | 提供版本、端点、配置、错误 |
| GitHub issues | 项目 issue | bug 和增强请求 | 能最小复现时 | 不把业务配置问题当 bug 提 |

## 11. 一句话总结

Getting Help 页真正想告诉你的是：Spring Authorization Server 的问题不能只按“登录接口问题”排查，而要按 Spring Security + OAuth2/OIDC 的分层模型去查；先看官方文档和 sample，再问社区，最后才是 GitHub issue。

## 12. 下一页学习重点

下一页是 Getting Started，开始进入真正的代码和配置。

你下一页要重点关注：

- Maven/Gradle 依赖怎么加；
- Spring Boot 自动配置会帮你做什么；
- 最小 Authorization Server 需要哪些 Bean；
- `SecurityFilterChain` 为什么通常要拆成两条；
- `RegisteredClientRepository` Demo 写法为什么不能直接生产用；
- `JWKSource` Demo 随机密钥为什么会导致重启后旧 Token 失效；
- 如何把官方最小 Demo 改造成 `conference-module-auth` 的雏形。
