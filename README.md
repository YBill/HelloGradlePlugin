# Android Gradle插件开发

#### 什么是 Gradle 插件

在 Android 开发中，我们最常见的 build.gradle 中的 apply plugin: 'com.android.application'，这个 apply plugin: 'com.android.application' 便是 Android 提供的用于构建APK的一个gradle插件。


#### Gradle 插件的编写方式

一般有如下三种方式编写Gradle插件：

- 构建脚本

> 您可以直接在构建脚本中包含插件的源代码。这样做的好处是插件会自动编译并包含在构建脚本的类路径中，而您无需执行任何操作。但是，该插件在构建脚本之外不可见，因此您不能在定义它的构建脚本之外重用该插件。

- buildSrc 项目

> 您可以将插件的源代码放在 rootProjectDir/buildSrc/src/main/java 目录中（rootProjectDir/buildSrc/src/main/groovy 或 rootProjectDir/buildSrc/src/main/kotlin 取决于您喜欢的语言）。Gradle 将负责编译和测试插件，并使其在构建脚本的类路径上可用。该插件对构建使用的每个构建脚本都是可见的。但是，它在构建之外不可见，因此您不能在定义它的构建之外重用插件。

- 独立项目

> 您可以为您的插件创建一个单独的项目。该项目生成并发布一个 JAR，然后您可以在多个构建中使用它并与他人共享。通常，这个 JAR 可能包含一些插件，或者将几个相关的任务类捆绑到一个库中。或者两者的某种组合。


下面分别用一个例子来说明在Android开发中三种编写Gradle插件的流程。



