# API列表

已集成swagger ui。

启动项目后，访问<http://localhost:8080/swagger-ui.html>,即可查看提供的所有API。

# 并发控制

同一账户同时进行多笔交易时，进行了并发控制。

通过通知等待机制实现的Allocator类一次性获取到要转入和转出的账户，并进行synchronized互斥锁定，从而继续进行交易动作。避免出现死锁问题。

# pom.xml 文件详解
- spring-boot-starter-data-jpa

  springboot启动脚手架，旨在简化基于JPA的数据访问层的开发
- spring-boot-starter-validation
- spring-boot-starter-web

  springboot启动脚手架，旨在简化WEB应用开发
- h2
- lombok
- springdoc-openapi-starter-webmvc-ui

  springboot集成OpenAPI的脚手架，方便用户生成、维护基于OpenAPI规范的API文档。
- spring-boot-starter-cache
