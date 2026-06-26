# 01. Overview

> 官方版本：Spring Authorization Server 1.5.8  
> 官方页面：https://docs.spring.io/spring-authorization-server/reference/overview.html  
> 本页定位：总览页，理解 Spring Authorization Server 是什么、适合什么场景、官方支持哪些能力。

## 1. 本页主要在教什么

这一页不是教你写代码，也不是讲具体配置，而是在回答三个问题：

1. Spring Authorization Server 是什么。
2. 什么情况下应该使用它，而不是直接使用 Keycloak、Auth0、企业统一身份平台等现成产品。
3. 它当前支持哪些 OAuth2 / OIDC 能力，包括授权模式、Token 格式、客户端认证方式、协议端点等。

放到学习路线里，这一页属于“整体定位 + 能力边界”。它决定你后面学习时应该把 Spring Authorization Server 看成什么：不是普通登录接口，不是资源服务器，也不是权限菜单系统，而是一个用于构建 OAuth2 Authorization Server 和 OIDC Provider 的基础框架。

## 2. 它属于 Authorization Server 的哪一部分

| 分类 | 说明 |
|---|---|
| 文档类型 | Overview / 总览 |
| 所属模块 | Authorization Server 整体介绍 |
| 是否涉及启动配置 | 否 |
| 是否涉及客户端注册 | 只列出能力，不讲具体注册方式 |
| 是否涉及 Token 签发 | 只列出支持的授权模式和 Token 格式 |
| 是否涉及 OIDC | 是，但只是说明支持 OpenID Connect 1.0 |
| 是否涉及 JWK | 是，但只列出 JWK Set Endpoint |
| 是否涉及端点 | 是，列出 Authorization Endpoint、Token Endpoint、JWK Set Endpoint 等 |
| 是否涉及数据库持久化 | 否 |
| 是否涉及扩展点 | 间接涉及，强调可定制，但未展开 |

这一页最重要的价值是帮你判断：`conference-module-auth` 这个独立鉴权中心是否适合基于 Spring Authorization Server 来做。

我的结论是：适合。

原因是你的项目不是简单登录，而是要把 OAuth2/OIDC、微服务 Gateway、Feign 服务间调用、system 模块 RBAC、多租户、审计日志整合到一起。Spring Authorization Server 的优势正好是“轻量、可定制、和 Spring Security / Spring Boot 体系结合紧密”。

## 3. 按小节逐段解释

## 3.1 Overview

官方 Overview 首先说明：这个站点包含 Spring Authorization Server 的参考文档和 How-to 指南。

这句话看似普通，但对学习方式很关键：

- Reference 是主线，解释模型、组件、端点、配置方式。
- How-to 是专题，解决具体生产问题，例如 PKCE、JPA、Redis、多租户、自定义 claims。

所以不要一上来只看 Getting Started，也不要只复制 Demo。正确学习方式应该是：先理解 Reference 的主线模型，再用 How-to 解决你的生产落地问题。

对你的项目来说：

- `Configuration Model` 决定 `conference-module-auth` 的 Spring Security 配置结构。
- `Core Model / Components` 决定 client、authorization、consent、token 等数据怎么入库。
- `Protocol Endpoints` 决定 Gateway、前端、Feign 调用 Authorization Server 时到底访问哪些端点。
- How-to 中的 PKCE、JPA、claims、多租户，是最贴近你项目的内容。

## 3.2 Introducing Spring Authorization Server

官方把 Spring Authorization Server 定义为一个框架，它提供 OAuth 2.1、OpenID Connect 1.0 以及相关规范的实现，并且构建在 Spring Security 之上，用来作为构建 OIDC Identity Provider 和 OAuth2 Authorization Server 产品的基础。

这句话要拆开理解。

### 3.2.1 它是 Authorization Server，不是 Resource Server

OAuth2 里常见角色有四个：

| 角色 | 在你的项目里对应什么 |
|---|---|
| Resource Owner | 登录用户，例如会议管理员、参会人、运营人员 |
| Client | `platform-admin`、`conference-portal`、`gateway`、`system`、`pay` 等 |
| Authorization Server | `conference-module-auth` |
| Resource Server | Gateway 以及下游业务服务 |

Spring Authorization Server 主要负责 Authorization Server 这一块：

- 处理授权请求；
- 处理登录后的授权码；
- 签发 access_token / refresh_token / id_token；
- 暴露 JWK Set；
- 暴露 OAuth2 / OIDC 元数据；
- 管理 client、authorization、consent 等授权相关数据。

它不应该直接变成你的 system 模块，也不应该直接承载全部菜单权限管理。

### 3.2.2 它构建在 Spring Security 之上

这意味着 SAS 不是绕开 Spring Security 自己搞一套过滤器，而是基于 Spring Security 的认证、过滤器链、AuthenticationProvider、AuthenticationConverter、SecurityFilterChain 等机制运行。

你后面会频繁看到这些类或概念：

| Spring Security 机制 | 在 SAS 中的作用 |
|---|---|
| `SecurityFilterChain` | 定义授权服务器端点使用哪条安全过滤器链 |
| `AuthenticationProvider` | 处理某一种认证或授权请求，例如授权码、client_credentials |
| `AuthenticationConverter` | 把 HTTP 请求转换成认证对象 |
| `AuthenticationManager` | 调度具体的 AuthenticationProvider |
| `OAuth2AuthorizationServerConfigurer` | SAS 的核心配置入口 |
| `RegisteredClientRepository` | 查询 client 注册信息 |
| `OAuth2AuthorizationService` | 管理授权记录和 Token 记录 |
| `OAuth2AuthorizationConsentService` | 管理用户授权同意记录 |

Overview 页没有展开这些类，但你要先有心理准备：后面真正落地生产时，本质是在 Spring Security 的扩展点上做 OAuth2/OIDC 协议实现和业务定制。

### 3.2.3 它支持 OAuth 2.1 和 OIDC 1.0

OAuth2 主要解决“授权访问资源”的问题。比如前端应用拿到 access_token 后访问会议系统接口。

OIDC 是建立在 OAuth2 之上的身份认证层，主要解决“当前登录用户是谁”的问题。它引入了 id_token、UserInfo Endpoint、Provider Configuration 等概念。

你的项目里建议这样理解：

| 协议 | 用途 | 是否建议使用 |
|---|---|---|
| OAuth2 Authorization Code + PKCE | 浏览器前端登录后换取 access_token | 建议使用 |
| OAuth2 client_credentials | 服务间调用 | 建议使用 |
| OIDC id_token | 前端识别登录用户身份 | 如果前端需要标准 OIDC 登录信息，可以使用 |
| OIDC UserInfo | 获取用户详情 | 可以对接 system 模块提供标准用户信息 |

注意：OAuth2 的 access_token 是给资源服务器看的，OIDC 的 id_token 是给客户端看的。不要把二者混用。

## 3.3 Use Cases

官方列出使用 Spring Authorization Server 的几个典型原因：

1. 需要对配置和高级定制拥有完全控制权。
2. 希望比商业化产品更轻量。
3. 希望节省软件授权或托管成本。
4. 希望开发阶段使用熟悉的 Spring 编程模型快速启动。

这几条其实是在告诉你：SAS 更适合“懂 Spring Security、需要深度定制、愿意自己承担鉴权中心建设”的团队。

### 3.3.1 什么情况下适合用 SAS

你的项目属于适合使用 SAS 的类型：

| 需求 | 为什么适合 SAS |
|---|---|
| 自己有 system 用户体系 | 可以自定义 UserDetailsService / AuthenticationProvider 对接 system |
| 自己有菜单、按钮 RBAC | 可以自定义 JWT claims 或 UserInfo 输出 |
| 微服务 Gateway 鉴权 | SAS 签发 JWT，Gateway 作为 Resource Server 校验 |
| 服务间调用 | SAS 原生支持 client_credentials |
| 多租户 | 可以通过自定义 claims、client 配置、tenant 上下文扩展 |
| 希望配置入库 | SAS 提供 `RegisteredClientRepository` 等接口可替换实现 |
| 希望审计日志 | 可以在登录、授权、Token 生成等扩展点记录审计 |

### 3.3.2 什么情况下不一定适合用 SAS

如果项目只是普通后台登录，或者公司已经有成熟统一身份平台，例如企业 SSO、Keycloak、Azure AD、Okta、Auth0，那么自建 SAS 不一定划算。

你需要自己负责：

- 密钥管理；
- client 生命周期；
- Token 生命周期；
- 授权同意页面；
- 登录安全策略；
- MFA / 风控；
- 审计；
- 高可用；
- 安全漏洞升级；
- 协议兼容性测试。

所以 SAS 不是“拿来就能当企业 IAM 平台”，它是“用 Spring 体系构建授权服务器产品的基础框架”。

## 3.4 Feature List

Feature List 是这一页最有价值的部分。它告诉你 SAS 支持哪些核心能力。

下面按类别解释。

## 4. 授权模式能力拆解

官方列出的 Authorization Grant 包括：

- Authorization Code
- Client Credentials
- Refresh Token
- Device Code
- Token Exchange

### 4.1 Authorization Code

Authorization Code 是浏览器端最推荐的登录授权模式。

在你的项目中，它对应：

```text
platform-admin / conference-portal
        │
        │ 1. 跳转到 auth 授权端点
        ▼
conference-module-auth
        │
        │ 2. 用户登录成功，返回 authorization_code
        ▼
前端 client
        │
        │ 3. 用 code + code_verifier 换 token
        ▼
conference-module-auth /oauth2/token
```

浏览器端必须配合 PKCE。因为浏览器前端属于 public client，无法安全保存 `client_secret`。

生产建议：

- `platform-admin` 和 `conference-portal` 注册为 public client；
- client authentication method 使用 `none`；
- grant type 使用 `authorization_code` 和 `refresh_token`；
- 必须开启 `requireProofKey(true)`；
- redirect_uri 必须精确配置，不能随便放通；
- 前端不要保存 client_secret；
- access_token 建议短生命周期；
- refresh_token 是否启用要结合前端安全策略决定。

### 4.2 Client Credentials

Client Credentials 用于服务访问服务，不代表某个用户，而代表某个客户端服务本身。

在你的项目中，它对应：

```text
conference-module-conference
        │ Feign 调用 pay
        │ 先用 client_id/client_secret 获取服务 Token
        ▼
conference-module-auth /oauth2/token
        │ 返回 service access_token
        ▼
Feign RequestInterceptor
        │ Authorization: Bearer <service-token>
        ▼
conference-module-pay
```

这种 Token 的 subject 通常不是用户 ID，而是 client_id 或服务身份。

生产建议：

- `gateway`、`system`、`conference`、`pay`、`infra` 注册为 confidential client；
- 使用 `client_secret_basic` 或更强方式；
- client_secret 必须加密或哈希存储，不要明文；
- 服务 Token scope 应该细分，例如 `system.read`、`pay.refund`；
- Feign 获取 Token 要做缓存，不能每次请求都调用 auth；
- 获取失败要统一异常处理和告警；
- 不要把用户 Token 当作所有服务间调用 Token。

### 4.3 Refresh Token

Refresh Token 用于在 access_token 过期后换取新的 access_token。

它在你的项目里主要用于浏览器端登录保持。

生产注意：

- refresh_token 生命周期要比 access_token 长，但不能无限长；
- public client 使用 refresh_token 时要非常谨慎；
- 可以使用 Refresh Token Rotation，降低泄漏风险；
- 用户改密码、禁用账号、退出登录时，需要考虑 refresh_token 失效；
- refresh_token 最好服务端可追踪、可撤销。

### 4.4 Device Code

Device Code 通常用于电视、IoT、CLI 等输入不方便的设备登录。

你的 conference 项目当前不一定需要。

可能场景：

- 会议现场大屏设备登录；
- 签到终端登录；
- 自助机登录；
- CLI 工具访问会议系统。

现阶段可以先不作为主线。

### 4.5 Token Exchange

Token Exchange 是用一个 Token 换另一个 Token 的机制，常见于复杂微服务、代理调用、跨系统委托。

你的项目后期可能会遇到类似需求：

- Gateway 拿用户 Token，换一个下游服务专用 Token；
- A 服务代表用户调用 B 服务，但希望 Token audience 限定为 B；
- 跨系统集成时，把外部 Token 换成内部 Token。

但第一阶段不建议直接上 Token Exchange。你先把用户 Token 和服务 Token 两条主线做好。

## 5. Token Formats 拆解

官方列出两种 Token 格式：

- Self-contained，也就是 JWT；
- Reference，也就是 Opaque Token。

### 5.1 JWT access_token

JWT 是自包含 Token。资源服务器拿到 JWT 后，可以通过 JWK 公钥本地验签，不必每次请求都访问授权服务器。

优点：

- 性能好；
- 适合 Gateway 高频校验；
- 适合微服务本地校验；
- 能携带必要 claims，例如 user_id、tenant_id、scope、authorities。

缺点：

- 签发后在过期前不容易立即失效；
- claims 不能放太多，否则 Token 过大；
- 权限变化后旧 Token 仍可能有效；
- 密钥轮换需要设计好 kid 和缓存策略。

你的项目建议：

- 用户 access_token 使用 JWT；
- 服务 access_token 使用 JWT；
- refresh_token 不建议使用 JWT 自包含，建议服务端持久化，便于撤销；
- JWT claims 只放稳定、必要、高频使用的信息。

### 5.2 Opaque Token

Opaque Token 是引用型 Token，本身只是一段随机字符串。资源服务器要通过 introspection endpoint 向授权服务器查询 Token 是否有效以及对应信息。

优点：

- 容易撤销；
- 服务端可控性强；
- Token 内容不会暴露给客户端。

缺点：

- 每次校验可能访问授权服务器；
- 对 auth 模块可用性和性能要求高；
- Gateway 高频场景下压力更大。

你的项目第一阶段更推荐 JWT access_token，不推荐所有请求都走 introspection。

## 6. Client Authentication 拆解

官方列出多种客户端认证方式：

- `client_secret_basic`
- `client_secret_post`
- `client_secret_jwt`
- `private_key_jwt`
- `tls_client_auth`
- `self_signed_tls_client_auth`
- `none`

### 6.1 `none`

`none` 表示 public client 不进行客户端密钥认证。

它不是“不安全随便放行”，而是配合 PKCE 使用。

你的项目中：

| client_id | 认证方式 |
|---|---|
| `platform-admin` | `none` + PKCE |
| `conference-portal` | `none` + PKCE |

生产注意：

- public client 不能配置 client_secret；
- 必须启用 PKCE；
- redirect_uri 必须严格匹配；
- 不要把 public client 当 confidential client 用。

### 6.2 `client_secret_basic`

这是服务端 confidential client 最常见方式。client_id 和 client_secret 通过 HTTP Basic 方式提交给 token endpoint。

你的项目中内部服务可以使用这种方式：

| client_id | 认证方式 |
|---|---|
| `gateway` | `client_secret_basic` |
| `system` | `client_secret_basic` |
| `conference` | `client_secret_basic` |
| `pay` | `client_secret_basic` |
| `infra` | `client_secret_basic` |

生产注意：

- 全站必须 HTTPS；
- client_secret 不允许明文存储；
- 不要把 secret 写进前端；
- 配置文件中的 secret 要通过配置中心密文、KMS、环境变量等方式管理；
- 数据库中最好存储 bcrypt/argon2 等编码后的 secret。

### 6.3 `client_secret_post`

这种方式把 client_secret 放在请求 body 里。一般不优先推荐，除非客户端限制不能使用 Basic。

生产建议：优先使用 `client_secret_basic`。

### 6.4 `client_secret_jwt` / `private_key_jwt`

这两种是更强的客户端认证方式。客户端用 JWT 证明自己的身份。

适合更高安全要求的服务间调用或第三方接入。

第一阶段你可以先不用，但后续如果有外部合作方接入，`private_key_jwt` 会比共享 secret 更好管理。

### 6.5 mTLS 相关认证

`tls_client_auth` 和 `self_signed_tls_client_auth` 用于双向 TLS 客户端认证。

这通常适合金融、政企、强合规场景。你的项目第一阶段可以不做，但要知道 SAS 是支持这个方向的。

## 7. Protocol Endpoints 拆解

Overview 页列出了一批协议端点。它们后面会在 Protocol Endpoints 页面详细展开。

### 7.1 OAuth2 Authorization Endpoint

通常是：

```text
/oauth2/authorize
```

用途：浏览器跳转到授权服务器，发起授权码流程。

你的项目中：

- `platform-admin` 点击登录；
- 跳转到 `conference-module-auth`；
- 用户登录成功后返回 authorization_code；
- 前端用 code 换 token。

### 7.2 OAuth2 Token Endpoint

通常是：

```text
/oauth2/token
```

用途：用 authorization_code、refresh_token、client_credentials 等 grant 换 Token。

你的项目中：

- 前端用 code + PKCE 换用户 Token；
- Feign 客户端用 client_credentials 换服务 Token；
- 前端或 BFF 用 refresh_token 刷新 Token。

### 7.3 JWK Set Endpoint

通常是：

```text
/oauth2/jwks
```

用途：暴露公钥，给 Resource Server 验证 JWT 签名。

你的项目中：

- Gateway 通过 issuer-uri 或 jwk-set-uri 获取公钥；
- 下游服务如果也做资源服务器，也可以获取公钥；
- 密钥轮换时要保证旧 Token 在过期前仍能被验证。

### 7.4 Authorization Server Metadata Endpoint

通常是：

```text
/.well-known/oauth-authorization-server
```

用途：暴露授权服务器的 issuer、authorization_endpoint、token_endpoint、jwks_uri 等元数据。

你的项目中 Gateway 和客户端可以通过 issuer-uri 自动发现配置。

### 7.5 OIDC Provider Configuration Endpoint

通常是：

```text
/.well-known/openid-configuration
```

用途：OIDC 客户端发现 Provider 配置。

如果你的前端或第三方系统按标准 OIDC 接入，这个端点很重要。

### 7.6 UserInfo Endpoint

通常是：

```text
/userinfo
```

用途：客户端拿 access_token 获取当前用户信息。

你的项目如果启用 OIDC，可以让 UserInfo 对接 `conference-module-system`，返回用户基础资料、租户信息、组织信息，但不建议返回完整菜单树和按钮权限。

### 7.7 Token Introspection Endpoint

用途：资源服务器查询 Token 是否有效。

如果你采用 JWT，本地验签即可，不需要每个请求 introspection。

Opaque Token 场景下才更常用。

### 7.8 Token Revocation Endpoint

用途：撤销 Token。

你的项目中可以用于：

- 用户退出登录；
- 管理员强制下线；
- client secret 泄漏后的 Token 清理；
- refresh_token 撤销。

### 7.9 Dynamic Client Registration Endpoint

用途：动态注册客户端。

你的项目是否使用要谨慎。对于后台运营配置 client，通常不一定要开放标准动态注册端点，可以自己做运营管理页面，写入 SAS 需要的 client 表。

## 8. 本页没有代码，但要提前理解的代码落点

Overview 页本身没有 Java 代码或 YAML 配置，但它后面会落到这些关键 Bean：

| 未来会遇到的类 / Bean | 作用 | conference 项目落点 |
|---|---|---|
| `SecurityFilterChain` | 授权服务器端点安全过滤器链 | `conference-module-auth` 安全配置 |
| `OAuth2AuthorizationServerConfigurer` | 启用和定制 SAS 协议端点 | 自定义端点行为、OIDC、异常处理 |
| `RegisteredClientRepository` | 查询客户端注册信息 | 入库，运营配置 client |
| `OAuth2AuthorizationService` | 保存授权与 Token | 入库，支持刷新、撤销、审计 |
| `OAuth2AuthorizationConsentService` | 保存用户授权同意 | 用户授权页面、scope consent |
| `JWKSource<SecurityContext>` | 提供签名密钥 | 密钥持久化和轮换 |
| `AuthorizationServerSettings` | 配置 issuer 和端点路径 | 生产必须固定 issuer |
| `OAuth2TokenCustomizer<JwtEncodingContext>` | 自定义 JWT claims | 写入 user_id、tenant_id、scope、authorities 等 |

## 9. Spring Boot 自动配置和手动配置的区别

这一页没有直接讲 Boot 自动配置，但你后面学习时要先建立这个区别。

### 9.1 自动配置

Spring Boot 会根据依赖和配置自动创建一部分默认 Bean，让你快速跑起来。

适合：

- Demo；
- 快速验证流程；
- 学习端点行为。

不适合：

- client 入库；
- 密钥管理；
- 权限 claims 定制；
- 多租户；
- 审计；
- 生产安全策略。

### 9.2 手动配置

生产环境通常要显式声明关键 Bean。

例如：

- 自定义 `RegisteredClientRepository`，从数据库读取 client；
- 自定义 `OAuth2AuthorizationService`，保存授权记录；
- 自定义 `JWKSource`，读取持久化密钥；
- 自定义 `OAuth2TokenCustomizer`，写入业务 claims；
- 自定义登录认证逻辑，对接 system 用户表；
- 自定义异常响应和审计日志。

你的项目最终应该以手动配置为主，自动配置只作为基础。

## 10. 放到我的 conference 项目里应该怎么设计

### 10.1 模块职责

`conference-module-auth` 应该只负责认证授权相关能力：

- 登录认证；
- OAuth2 授权流程；
- Token 签发；
- JWK 管理；
- OIDC 元数据；
- client 注册查询；
- authorization / consent 管理；
- Token 自定义 claims；
- 登录与 Token 审计。

`conference-module-system` 继续负责业务权限：

- 用户；
- 角色；
- 菜单；
- 按钮；
- 部门；
- 租户；
- 岗位；
- 数据权限。

不要把 system 的菜单权限管理整体搬到 auth。auth 只需要在登录和签发 Token 时读取必要信息。

### 10.2 public client 注册

浏览器端注册为 public client：

| client_id | 类型 | grant type | client auth | PKCE | redirect_uri |
|---|---|---|---|---|---|
| `platform-admin` | public | `authorization_code`, `refresh_token` | `none` | required | 管理后台回调地址 |
| `conference-portal` | public | `authorization_code`, `refresh_token` | `none` | required | 门户前端回调地址 |

生产注意：

- redirect_uri 必须精确；
- 不建议通配过大；
- 前端不保存 secret；
- PKCE 使用 S256；
- access_token 短有效期；
- refresh_token 是否发给浏览器要评估风险。

### 10.3 confidential client 注册

内部服务注册为 confidential client：

| client_id | 类型 | grant type | client auth | scope 示例 |
|---|---|---|---|---|
| `gateway` | confidential | `client_credentials` | `client_secret_basic` | `gateway.internal` |
| `system` | confidential | `client_credentials` | `client_secret_basic` | `system.read`, `system.write` |
| `conference` | confidential | `client_credentials` | `client_secret_basic` | `conference.read`, `conference.write` |
| `pay` | confidential | `client_credentials` | `client_secret_basic` | `pay.read`, `pay.write`, `pay.refund` |
| `infra` | confidential | `client_credentials` | `client_secret_basic` | `infra.file`, `infra.message` |

### 10.4 哪些配置应该入库

建议入库：

| 配置 | 是否入库 | 原因 |
|---|---:|---|
| client_id | 是 | 运营可配置 |
| client_secret hash | 是 | 服务端 client 凭证 |
| grant_types | 是 | 不同 client 不同授权方式 |
| redirect_uris | 是 | 前端回调地址需要管理 |
| scopes | 是 | 服务权限边界 |
| token_settings | 是 | 不同 client Token 生命周期不同 |
| client_settings | 是 | 是否需要 PKCE、是否需要授权同意 |
| authorization 记录 | 是 | refresh、撤销、审计都需要 |
| consent 记录 | 是 | 用户授权同意需要追踪 |
| JWK key metadata | 是 | 至少要持久化密钥或密钥引用 |

### 10.5 哪些配置放配置文件

建议放配置文件或配置中心：

| 配置 | 原因 |
|---|---|
| issuer | 环境级配置，必须稳定 |
| 数据库连接 | 基础设施配置 |
| Redis 地址 | 基础设施配置 |
| KMS / Vault 地址 | 密钥管理基础设施 |
| 是否启用 OIDC | 环境功能开关 |
| Gateway 内网信任配置 | 部署环境相关 |
| Token 默认生命周期 | 可作为默认值，client 可覆盖 |

### 10.6 Gateway 怎么配合

Gateway 应作为 Resource Server：

- 校验用户 JWT；
- 校验 issuer；
- 校验 audience；
- 校验 scope；
- 解析 `sub`、`user_id`、`tenant_id`；
- 删除外部传入的伪造上下文头；
- 注入可信的 `X-User-Id`、`X-Tenant-Id`、`X-Operator-Id`；
- 下游服务只信任来自 Gateway 的这些头。

### 10.7 OpenFeign 怎么配合

Feign 内部服务调用建议：

- 使用 Spring Security OAuth2 Client 获取 client_credentials Token；
- 用 `RequestInterceptor` 给请求加 `Authorization: Bearer <service-token>`；
- Token 缓存到过期前，不要每次请求都重新获取；
- 可以同时传递用户上下文头和服务 Token：

```text
Authorization: Bearer <service-token>
X-User-Id: 10001
X-Tenant-Id: 20001
X-Operator-Id: 10001
X-Request-Id: xxx
```

其中：

- `Authorization` 证明调用方服务是谁；
- `X-User-Id` 等上下文说明这次操作代表哪个用户；
- 下游服务要区分“服务身份”和“用户上下文”。

## 11. 容易踩坑的地方

| 坑 | 现象 | 原因 | 解决方式 |
|---|---|---|---|
| redirect_uri 不匹配 | 前端登录后报 invalid_redirect_uri | 请求里的 redirect_uri 和注册的不完全一致 | 入库时精确配置，区分本地、测试、生产环境 |
| issuer 不一致 | Gateway 校验 JWT 失败 | Token 里的 `iss` 和 Resource Server 配置不同 | 生产固定 issuer，例如 `https://auth.example.com` |
| JWK 重启失效 | 服务重启后旧 Token 全部验签失败 | Demo 随机生成密钥 | 密钥持久化，使用 kid，支持轮换 |
| public client 配了 secret | 前端需要保存 secret，存在泄漏风险 | 把浏览器应用误认为 confidential client | public client 使用 `none` + PKCE |
| PKCE 没开启 | 授权码被截获后可能被换 Token | public client 缺少 code_verifier 校验 | `requireProofKey(true)`，使用 S256 |
| scope 配置混乱 | Token 有 scope，但接口不认 | client、auth、gateway、服务端权限模型没统一 | 设计统一 scope 命名规范 |
| refresh_token 不生效 | code 能换 access_token，但没有 refresh_token | client 未启用 refresh_token 或 scope/setting 不满足 | 注册 client 时加入 refresh_token grant，检查 TokenSettings |
| Token 过大 | 请求头过大或网关拒绝 | JWT 塞入完整菜单、按钮树 | JWT 只放必要 claims，菜单权限按需查询或缓存 |
| 过滤器链顺序错误 | 授权端点 404、登录循环、token endpoint 被表单登录拦截 | Authorization Server 的 SecurityFilterChain 顺序不对 | 授权服务器链使用高优先级，普通登录链分开 |
| client_secret 明文 | 数据泄漏后所有服务凭证暴露 | 直接明文入库或写配置 | secret hash 存储，配置中心密文，定期轮换 |
| 服务 Token 每次请求都获取 | auth 压力大，接口变慢 | Feign 拦截器没有缓存 Token | 使用 OAuth2AuthorizedClientManager 或自定义缓存 |
| 下游服务信任外部头 | 用户可伪造 `X-User-Id` | Gateway 没清理外部传入头 | Gateway 先删除再注入，内网禁止绕过 Gateway |

## 12. 生产级建议

### 12.1 客户端注册入库

不要使用 Demo 里的内存 client。

建议设计后台运营页面管理 client：

- client_id；
- client_name；
- client_type：public / confidential；
- client_secret_hash；
- authentication_methods；
- authorization_grant_types；
- redirect_uris；
- post_logout_redirect_uris；
- scopes；
- access_token_ttl；
- refresh_token_ttl；
- require_pkce；
- require_consent；
- enabled；
- created_at / updated_at。

### 12.2 密钥管理和轮换

生产不要启动时随机生成 RSA key。

建议：

- 使用固定 issuer；
- 使用持久化 RSA / EC 密钥；
- 每个密钥有 kid；
- 新密钥用于签发，旧密钥保留到旧 Token 全部过期；
- JWK Set 暴露当前有效公钥集合；
- 私钥可放 KMS / Vault / 数据库加密字段 / 文件密钥库；
- 记录密钥创建时间、启用时间、停用时间。

### 12.3 JWT claims 设计

建议用户 Token claims：

| claim | 示例 | 说明 |
|---|---|---|
| `sub` | `10001` | 用户主体 ID，稳定唯一 |
| `user_id` | `10001` | 业务用户 ID，可和 sub 一致 |
| `tenant_id` | `20001` | 当前租户 |
| `client_id` | `platform-admin` | 当前客户端 |
| `scope` | `openid profile conference.read` | OAuth2 scope |
| `authorities` | `sys:user:list` | 少量高频按钮权限，可选 |
| `roles` | `ADMIN` | 粗粒度角色，可选 |
| `aud` | `conference-api` | Token 预期接收方 |
| `iss` | `https://auth.xxx.com` | 签发者 |

不建议放：

- 完整菜单树；
- 完整按钮权限列表过大版本；
- 用户敏感信息；
- 手机号、身份证、地址等隐私信息；
- 经常变化的数据权限规则。

### 12.4 RBAC 权限衔接

system 模块继续作为权限数据源。

推荐策略：

- 登录时读取用户基础信息和租户信息；
- Token 中只放必要身份信息和少量权限摘要；
- 菜单树由前端登录后单独请求 system 接口获取；
- 按钮权限可以按系统规模选择放 Token 或接口查询；
- 权限变化后，可以通过缩短 access_token TTL、权限版本号、用户强制下线机制解决旧 Token 问题。

### 12.5 Gateway 资源服务器配置

Gateway 应该：

- 使用 issuer-uri 自动发现 JWK；
- 校验签名；
- 校验 issuer；
- 校验 audience；
- 校验 Token 过期时间；
- 将 scope/authority 转成网关可用权限；
- 清理外部伪造头；
- 注入可信上下文；
- 对外只暴露 Gateway，不让外部直接访问下游服务。

### 12.6 Feign 服务间 Token

建议：

- 每个服务一个 confidential client；
- Feign 拦截器按目标服务选择 registrationId；
- 默认 registrationId 可以等于 `spring.application.name`；
- 第三方 Feign Client 必须排除，不要带内部 Token；
- 获取 Token 失败统一抛出业务异常并告警；
- 日志中不要打印 access_token 和 client_secret；
- 同时传递 traceId / requestId / tenantId / userId。

### 12.7 Token 生命周期

建议初始值：

| Token | 建议有效期 | 说明 |
|---|---:|---|
| 用户 access_token | 15-30 分钟 | 降低泄漏风险 |
| 用户 refresh_token | 7-30 天 | 结合安全策略 |
| 服务 access_token | 5-30 分钟 | Feign 缓存即可 |
| authorization_code | 1-5 分钟 | 一次性使用 |
| device_code | 5-15 分钟 | 暂不作为主线 |

## 13. 本页核心类 / 配置 / 概念总结表

| 概念 | 它是什么 | 解决什么问题 | 在流程中的位置 | 生产建议 |
|---|---|---|---|---|
| Spring Authorization Server | 构建 OAuth2/OIDC 授权服务器的框架 | 自建授权中心 | `conference-module-auth` | 适合深度定制，不能只复制 Demo |
| OAuth2 Authorization Server | 负责授权和 Token 签发的角色 | 给 client 发 access_token | auth 模块 | 和 Resource Server 职责分离 |
| OIDC Provider | 提供用户身份认证能力 | 给客户端确认用户是谁 | auth 模块 | 需要 id_token / UserInfo 时启用 |
| Authorization Code | 浏览器登录授权模式 | 前端安全获取 Token | authorize + token endpoint | 必须配合 PKCE |
| PKCE | public client 的授权码保护机制 | 防止授权码被截获滥用 | code 换 token 时校验 | public client 必开 |
| Client Credentials | 服务间调用授权模式 | 服务以自己身份访问服务 | token endpoint | 服务 Token 缓存，scope 细分 |
| Refresh Token | 刷新 access_token 的凭证 | 登录保持 | token endpoint | 可撤销，可轮换，谨慎发给浏览器 |
| JWT | 自包含 Token | 本地验签，提高性能 | Gateway / Resource Server | 不放大对象，不放敏感信息 |
| Opaque Token | 引用型 Token | 服务端可控、易撤销 | introspection | 高频网关场景慎用 |
| JWK Set Endpoint | 公钥集合端点 | Resource Server 验签 | `/oauth2/jwks` | 密钥持久化，支持轮换 |
| Authorization Endpoint | 发起授权请求 | 用户登录和授权 | `/oauth2/authorize` | redirect_uri 精确匹配 |
| Token Endpoint | 换取 Token | code、refresh、client_credentials 换 Token | `/oauth2/token` | 客户端认证和审计要做好 |
| Metadata Endpoint | 授权服务器元数据 | 自动发现 issuer、端点、jwks_uri | well-known endpoint | issuer 必须稳定 |
| UserInfo Endpoint | OIDC 用户信息端点 | 客户端获取用户资料 | OIDC | 对接 system，避免返回过多权限 |
| Token Revocation | Token 撤销端点 | 退出登录、强制下线 | revocation endpoint | refresh_token 撤销很重要 |
| Dynamic Client Registration | 动态注册 client | 标准化 client 注册 | registration endpoint | 内部运营配置可不开放标准端点 |

## 14. 一句话总结

Spring Authorization Server 的核心定位不是“帮你写登录接口”，而是让你用 Spring Security 体系构建一个可定制的 OAuth2/OIDC 授权服务器；对 conference 项目来说，它最适合承担独立的 `conference-module-auth`，但用户、角色、菜单、按钮权限仍应由 system 模块管理。

## 15. 下一页学习重点

下一页是 Getting Help。它内容不会很复杂，重点不是技术配置，而是了解官方样例、问题入口和后续遇到坑时应该去哪里查。

真正要重点学习的是再下一页 Getting Started：

- 最小授权服务器怎么启动；
- Demo 中哪些 Bean 是必须的；
- 哪些配置只是教学用；
- 为什么生产环境不能直接使用内存 client 和随机 JWK；
- Spring Boot 自动配置和手写配置如何配合。
