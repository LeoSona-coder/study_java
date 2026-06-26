# Spring Authorization Server 学习与 conference 项目落地

> 基于 Spring Authorization Server 官方参考文档整理。  
> 当前学习基准版本：Spring Authorization Server 1.5.8。  
> 官方文档入口：https://docs.spring.io/spring-authorization-server/reference/

这个目录不是官方文档翻译，而是面向 Java 后端开发者的中文学习笔记：

- 用中文重新组织 Spring Authorization Server 的官方设计思想。
- 结合 Spring Boot 3、Spring Security、Gateway、OpenFeign、JPA/MyBatis 的微服务项目落地。
- 以 `conference-module-auth` 独立鉴权中心为目标进行分析。
- 浏览器端重点使用 Authorization Code + PKCE。
- 服务间调用重点使用 client_credentials。
- 用户、角色、菜单、按钮权限仍由 `conference-module-system` 维护。
- Gateway 负责校验用户 JWT，并向下游服务传递可信用户上下文。
- OpenFeign 负责给内部服务调用追加服务 Token。

## 文档结构

```text
spring-authorization-server-study/
├── README.md
├── docs/
│   ├── 00-reading-plan.md
│   ├── 01-overview.md
│   ├── 02-getting-help.md
│   ├── 03-getting-started.md
│   ├── 04-configuration-model.md
│   ├── 05-core-model-components.md
│   ├── 06-protocol-endpoints.md
│   └── how-to/
│       ├── 01-spa-pkce.md
│       ├── 02-social-login.md
│       ├── 03-extension-grant.md
│       ├── 04-multitenancy.md
│       ├── 05-oidc-userinfo.md
│       ├── 06-jpa-core-services.md
│       ├── 07-redis-core-services.md
│       ├── 08-authorities-claims.md
│       └── 09-dynamic-client-registration.md
└── examples/
    ├── conference-module-auth/
    ├── gateway-resource-server/
    └── openfeign-client-credentials/
```

## 每页笔记固定结构

每一页都会尽量保持一致的阅读结构：

1. 本页主要在教什么。
2. 它属于 Authorization Server 的哪一部分。
3. 按官方小节逐段解释。
4. 关键类、配置项、Bean、接口、端点拆解。
5. 代码或配置重点说明。
6. 放到 conference 项目里应该怎么设计。
7. 容易踩坑的地方。
8. 生产级建议。
9. 总结表。
10. 一句话结论。
11. 下一页学习重点。

## conference 项目目标架构

```text
                ┌──────────────────────────┐
                │ platform-admin / portal   │
                │ Authorization Code + PKCE │
                └─────────────┬────────────┘
                              │
                              ▼
┌──────────────────────────────────────────────────┐
│ Gateway                                           │
│ - 校验用户 JWT                                    │
│ - 注入 X-User-Id / X-Tenant-Id / X-Operator-Id     │
│ - 路由到下游资源服务                              │
└───────────────────┬──────────────────────────────┘
                    │
                    ▼
┌──────────────────────────────────────────────────┐
│ Resource Services                                 │
│ system / conference / pay / infra                 │
│ - 只信任 Gateway 传入的用户上下文                 │
│ - 内部调用通过 Feign 追加服务 Token               │
└───────────────────┬──────────────────────────────┘
                    │ client_credentials
                    ▼
┌──────────────────────────────────────────────────┐
│ conference-module-auth                            │
│ - OAuth2 / OIDC Authorization Server              │
│ - Token 签发                                      │
│ - JWK Set                                         │
│ - RegisteredClient 入库                           │
│ - 对接 conference-module-system 用户与 RBAC        │
└──────────────────────────────────────────────────┘
```

## 当前进度

- [x] 00. 阅读计划
- [x] 01. Overview
- [x] 02. Getting Help
- [ ] 03. Getting Started
- [ ] 04. Configuration Model
- [ ] 05. Core Model / Components
- [ ] 06. Protocol Endpoints
- [ ] How-to Guides
