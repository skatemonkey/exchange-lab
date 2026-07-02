# DDD Study: Four-Layer Code Structure

> **Table of Contents**
>
> - [1. Code Structure](#1-code-structure)

## 1. Code Structure

```text
application
    event      -> event-related application code
                  事件相关的应用层代码
        publish    -> event publishing code
                      事件发布相关代码
        subscribe  -> event subscription / event handling code
                      事件订阅或事件处理相关代码
    service    -> application services
                  应用服务；组合、编排领域服务或外部应用服务，对外提供较粗粒度能力
domain
    aggregate00  -> aggregate package; named by real aggregate name
                    聚合包；可以根据实际聚合名称命名
                    defines aggregate root, entities, value objects, domain services,
                    and boundaries inside the aggregate
                    在聚合内定义聚合根、实体、值对象、领域服务之间的关系和边界
        entity      -> aggregate root, entities, value objects, and factory code
                       聚合根、实体、值对象、工厂相关代码
                       entity uses rich model; entity-related business logic lives here
                       实体使用充血模型，同一实体相关业务逻辑在实体类中实现
        event       -> domain events and event-related domain behavior
                       领域事件实体，以及与事件活动相关的业务逻辑
        repository  -> aggregate query and persistence contract
                       聚合的查询或持久化领域对象代码，通常包含仓储接口和实现方法
        service     -> domain service for multi-entity business logic
                       领域服务；多个实体组合出来的一段业务逻辑
    aggregate01
        entity
        event
        repository
        service
    aggregate02
infrastructure
    config     -> configuration-related code
                  配置相关代码
    util       -> infrastructure utility/basic resource code
                  平台、开发框架、消息、数据库、缓存、文件、总线、网关、第三方类库、
                  通用算法等基础代码；可以按资源类别建立不同子目录
        api       -> external API integration utilities
                    外部 API 集成相关基础代码
        driver    -> driver/client integration utilities
                    驱动或客户端集成相关基础代码
        eventbus  -> event bus utilities
                    事件总线相关基础代码
        mq        -> message queue utilities
                    消息队列相关基础代码
interfaces
    assembler  -> converts between DTO and domain objects
                  实现 DTO 与领域对象之间的相互转换和数据交换
    dto        -> data transfer object; no business logic
                  数据传输载体，内部不包含业务逻辑
    facade     -> coarse-grained API that delegates user requests to application services
                  提供较粗粒度的调用接口，将用户请求委派给一个或多个应用服务处理
```

Note: event handling may call domain logic, but the core business rules should
still live in the domain layer.
