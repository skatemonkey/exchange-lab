# DDD Study: Four-Layer Code Structure

> **Table of Contents**
>
> - [1. Code Structure](#1-code-structure)
> - [2. Another Code Structure Example](#2-another-code-structure-example)

## 1. Code Structure

```text
application     -> application layer (应用层)
    event      -> event-related application code
                  事件相关的应用层代码
        publish    -> event publishing code
                      事件发布相关代码
        subscribe  -> event subscription / event handling code
                      事件订阅或事件处理相关代码
    service    -> application services
                  应用服务；组合、编排领域服务或外部应用服务，对外提供较粗粒度能力
domain          -> domain layer (领域层)
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
infrastructure  -> infrastructure layer (基础设施层)
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
interfaces      -> interfaces layer (接口层)
    assembler  -> converts between DTO and domain objects
                  实现 DTO 与领域对象之间的相互转换和数据交换
    dto        -> data transfer object; no business logic
                  数据传输载体，内部不包含业务逻辑
    facade     -> coarse-grained API that delegates user requests to application services
                  提供较粗粒度的调用接口，将用户请求委派给一个或多个应用服务处理
```

- A higher layer can call any lower layer, not only the layer directly below it.
  Example: `interfaces` can call `application`, `domain`, or `infrastructure`.

## 2. Another Code Structure Example

```text
com.mashibing.ddd
    apis                         -> API interface layer (API 接口层)
        model                    -> view/data model, usually VO/DTO
                                    视图模型、数据模型定义，通常是 VO/DTO
        assembler                -> converts API model <-> domain model
                                    装配器，实现 apiModel <=> domainModel 转换
        controller               -> REST controller
                                    控制器，对外提供 RESTful 接口

    application                  -> application layer (应用层)
        service                  -> application service, not core domain service
                                    应用服务，非核心服务
        task                     -> task definition, coordinates domain models
                                    任务定义，协调领域模型
        ...                      -> others
                                    其他

    domain                       -> domain layer (领域层)
        common                   -> shared code extraction, only valid inside domain
                                    公共代码抽取，限于领域层有效
        events                   -> domain events
                                    领域事件
        model                    -> domain model
                                    领域模型
            dict                 -> domain sub-module
                                    领域划分的模块，可理解为子领域划分
                DictVo.java      -> domain value object
                                    领域值对象
                DictEntity.java  -> domain entity, rich domain model
                                    领域实体，充血的领域模型
                DictAgg.java     -> domain aggregate, usually entity collection with aggregate root
                                    领域聚合，通常表现为实体的聚合，需要有聚合根
                DictService.java -> domain service for logic that does not fit above models
                                    领域服务，不能归入上述模型的逻辑可放在这里
            xxx
                xxxEntity.java
                bbbAgg.java
                cccAgg.java
        service                  -> domain service category
                                    领域服务分类，不能归属于具体领域模型的行为
        factory                  -> factory for complex domain object creation
                                    工厂类，负责复杂领域对象创建，封装细节

    infrastructure               -> infrastructure layer (基础设施层)
        persistent               -> persistence mechanism
                                    持久化机制
            po                   -> persistence object
                                    持久化对象
            repository           -> repository interface and implementation, can integrate ORM
                                    仓储类，持久化接口和实现，可与 ORM 映射框架结合
        general                  -> general technical support for other layers
                                    通用技术支持，向其他层输出通用服务
            config               -> configuration classes
                                    配置类
            toolkit              -> utility/tool classes
                                    工具类
            common               -> basic common modules
                                    基础公共模块等

    resources
        statics                  -> static resources
                                    静态资源
        template                 -> system pages/templates
                                    系统页面
        application.yml          -> global configuration file
                                    全局配置文件
```
