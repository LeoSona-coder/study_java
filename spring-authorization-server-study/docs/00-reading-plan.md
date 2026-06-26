# 00. Spring Authorization Server 官方文档阅读计划

> 官方文档入口：https://docs.spring.io/spring-authorization-server/reference/  
> 当前基准版本：Spring Authorization Server 1.5.8  
> 学习目标：从官方设计思想出发，落地到 `conference-module-auth` 生产级鉴权中心。

## 1. 为什么要这样读

Spring Authorization Server 不是一个简单的“登录接口框架”。它更像是 Spring Security 生态中用于构建 OAuth2 Authorization Server / OIDC Provider 的基础设施。

如果只看 Demo，很容易把它理解成：

- 配一个 `SecurityFilterChain`；
- 写一个 `RegisteredClientRepository`；
- 生成一个 JWK；
- 前端跳转登录后拿 Token。

但生产环境真正困难的地方不在 Demo，而在这些问题：

- client 注册信息如何入库，如何让运营人员配置；
- public client 和 confidential client 如何区分；
- Authorization Code + PKCE 如何给浏览器前端使用；
- client_credentials 如何给服务间调用使用；
- JWT 里放哪些 claims，哪些权限不应该放；
- RBAC 菜单、按钮权限如何从 system 模块衔接到 Token；
- Gateway 只校验用户 Token 还是也校验服务 Token；
- Feign 获取服务 Token 失败如何统一处理；
- JWK 如何持久化和轮换；
- Refresh Token 生命周期怎么控制；
- 多租户上下文、审计日志如何设计。

所以这套笔记会按“官方文档主线 + conference 项目落地”的方式读。

## 2. 官方主线阅读顺序

| 顺序 | 页面 | 官方定位 | 学习重点 | conference 项目关注点 |
|---:|---|---|---|---|
| 1 | Overview | Introduction, use cases and feature list | 先确认 SAS 的定位和能力边界 | 是否适合自建 `conference-module-auth` |
| 2 | Getting Help | samples、questions、issues | 官方样例和问题入口 | 后续遇到问题如何查官方样例 |
| 3 | Getting Started | 依赖、系统要求、第一个应用 | 跑通最小授权服务器 | 哪些是 Demo，哪些不能生产用 |
| 4 | Configuration Model | 默认配置与自定义配置 | 最重要的配置模型 | `SecurityFilterChain`、`AuthorizationServerSettings`、端点启用 |
| 5 | Core Model / Components | 核心领域模型和组件接口 | 生产落地核心 | 客户端、授权、Token、Consent 入库与自定义 |
| 6 | Protocol Endpoints | OAuth2 / OIDC 端点实现 | 请求如何进入授权服务器 | 授权端点、Token 端点、JWK、元数据、UserInfo |

## 3. How-to Guides 阅读顺序

官方 How-to 不是全部同等重要。结合 conference 项目，我建议按下面顺序读：

| 优先级 | 页面 | 为什么重要 |
|---:|---|---|
| 1 | Authenticate using a Single Page Application with PKCE | 直接对应 `platform-admin`、`conference-portal` 这类浏览器前端 |
| 2 | Implement core services with JPA | 直接对应 RegisteredClient、Authorization、Consent 入库 |
| 3 | Add authorities as custom claims in JWT access tokens | 直接对应 system 模块 RBAC 权限衔接 |
| 4 | Implement Multitenancy | 直接对应 SaaS 多租户上下文 |
| 5 | Implement core services with Redis | 可用于理解 Redis 持久化/缓存的取舍 |
| 6 | Customize the OIDC UserInfo response | 如果使用 OIDC，需要输出用户信息 |
| 7 | Register a client dynamically | 如果做运营端动态注册 client，需要重点看 |
| 8 | Implement an Extension Authorization Grant Type | 暂时不是主线，除非要做短信码、自定义 grant |
| 9 | Authenticate using Social Login | 暂时不是主线，除非要接微信、GitHub、Google 登录 |

## 4. 每页分析模板

后续每一页都会尽量按下面模板整理：

```markdown
# xx. 页面标题

> 官方版本：Spring Authorization Server 1.5.8
> 官方页面：xxx.html

## 1. 本页主要在教什么
## 2. 它属于 Authorization Server 的哪一部分
## 3. 按小节逐段解释
## 4. 关键类 / 配置项 / Bean / 端点拆解
## 5. 代码重点拆解
## 6. 放到我的 conference 项目里应该怎么设计
## 7. 容易踩坑的地方
## 8. 生产级建议
## 9. 总结表
## 10. 一句话总结
## 11. 下一页学习重点
```

## 5. conference 项目初始设计假设

### 5.1 模块边界

| 模块 | 责任 |
|---|---|
| `conference-module-auth` | OAuth2/OIDC 授权服务器，负责登录认证、授权、Token 签发、JWK、元数据端点 |
| `conference-module-system` | 用户、组织、角色、菜单、按钮、租户、部门等业务权限数据 |
| `conference-gateway` | Resource Server，校验外部用户 JWT，注入可信用户上下文 |
| `conference-module-conference` | 会议业务资源服务，消费 Gateway 传递的用户上下文 |
| `conference-module-pay` | 支付业务资源服务，消费用户上下文，也支持服务间 Token |
| `conference-module-infra` | 文件、消息、基础设施能力，内部服务调用居多 |

### 5.2 客户端类型

| client_id | 类型 | 授权方式 | 是否需要 client_secret | 用途 |
|---|---|---|---|---|
| `platform-admin` | public client | authorization_code + PKCE | 否 | 管理后台浏览器端 |
| `conference-portal` | public client | authorization_code + PKCE | 否 | 会议门户前端 |
| `gateway` | confidential client | client_credentials | 是 | Gateway 内部服务身份 |
| `system` | confidential client | client_credentials | 是 | system 服务间调用 |
| `conference` | confidential client | client_credentials | 是 | conference 服务间调用 |
| `pay` | confidential client | client_credentials | 是 | pay 服务间调用 |
| `infra` | confidential client | client_credentials | 是 | infra 服务间调用 |

### 5.3 Token 类型建议

| Token | 建议格式 | 原因 |
|---|---|---|
| 用户 access_token | JWT | Gateway 和资源服务可本地校验，性能好 |
| 服务 access_token | JWT | 服务间调用频繁，适合本地校验 |
| refresh_token | 不透明随机值或服务端持久化 | 便于吊销和生命周期控制 |
| authorization_code | 服务端存储短期值 | 标准授权码流程需要短生命周期和一次性使用 |

## 6. 总体生产原则

1. Demo 中内存注册 client 的方式，只适合学习，不适合生产。
2. Demo 中启动时随机生成 JWK 的方式，只适合学习，不适合生产。
3. 用户权限数据不要全部塞进 JWT，避免 Token 过大和权限变更不及时。
4. Gateway 校验用户 Token 后，下游服务应信任 Gateway 注入的用户上下文，但要防止外部绕过 Gateway 直接访问内网服务。
5. 服务间调用建议用 `client_credentials`，不要复用用户 Token 去做所有内部调用。
6. 前端浏览器应用应按 public client 处理，使用 Authorization Code + PKCE，不应持有 client_secret。
7. 客户端配置、Token 生命周期、scope、redirect_uri、授权方式，应该支持入库和运营配置。
8. 密钥要持久化、区分 kid、支持轮换。
9. 审计日志要记录登录、授权、Token 签发、Token 刷新、Token 撤销、服务间调用失败等关键事件。

## 7. 下一步

从 `01-overview.md` 开始，先理解 Spring Authorization Server 的定位、适用场景和官方支持能力。
