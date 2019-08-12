# 说明

本项目是 Psy4J(http://github.com/corkine/psy4j) 的孵化项目，也是王书恒和张双伟的实验程序项目。

Psy4J 框架主要依赖 Logback 日志框架、JavaFx GUI 框架。实验程序分析、Psy4J 的实用工具主要依赖 Scala 语言的集合类库，同时采用 Scala 语言编写。整个项目采用 Maven 进行构建。没有完全并且充分的单元测试。

其中王书恒的项目在 /src/main/java/com.mazhangjing.wsh 目录下，张双伟的项目在 /src/main/java/com.mazhangjing.zsw 目录下，项目共用的代码，也就是 Psy4J 框架的程序在 /src/main/java/com.mazhangjing.lab 下。

Psy4J 的初始版本包括了定义 Screen、Trial、Experiment 三个抽象类和一个 Main JavaFx 运行环境的实现的 GUI 主窗口类。在正式版本中，大致保留了 Screen、Trial、Experiment 的构造，不过为了尽可能的便于和 JavaFx 集成，提供了 ExperimentRunner 的实验运行类，其中包含了 Experiment 的所有对象信息，通过反射查找类名创建 Experiment 以及定义可以全局回调其 API 的事件发生源线程 EventMaker 接口。同时，Main 被拆分为 ExperimentHelper 接口和 SimpleExperimentHelperImpl 实现。在任意 JavaFx 程序中，只用在合适的实际创建 Stage，并且调用 ExperimentHelper 的 API 初始化所有实验内容即可。

Psy4J 是我个人在学习完 Java、设计模式、Scala 之后为了解决心理学编程面临的多变量易错、反应时精度不高、命令式而非面向对象编程等等问题而写的一个类库，在半年的时间里，Psy4J 面临了很多的问题，比如多线程并发和时间精度控制，以及各种类的设计问题。这些问题现在都差不多解决了，我十分感谢王书恒和张双伟同学的耐心和选择，而没有使用更加成熟的 MATLAB 和 PTB —— 尽管后者更简单，不容易出错，但是它们代表着我厌恶的很多东西 —— 充斥着变量的复杂脚本、难以调试的开发体验、不经过测试就运行的不良习惯，以及脚本语言缓慢并且愚蠢的 API 设计。MATLAB 是一个优秀的工程平台，PTB 则是很棒的基于 OpenGL 的封装，Psy4J 不能，或许永远不能有那样的受众 —— 大部分原因来自于 Scala 的复杂且优美的正交特性导致的学习难度、面向对象思想的难以理解。但是，就我自己而言，Psy4J 提供的 API 很好的帮助我解决了序列定时带有事件的刺激呈现，得益于 OpenJFX（以及其默认的 JavaFx 实现） 的能力，Psy4J 才得以达到我想要的目标 —— 一致、跨平台、开发快速、运行快速、高精度、理解容易、结构清晰、容易复用、以及因为 Scala 精妙的工具集和静态类型带来的更少 Bug 问题。

2019-04-19 Corkine @ CCNU

