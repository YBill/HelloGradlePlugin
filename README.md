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


下面分别实现这三种编写 Gradle 插件的流程。


##### 构建脚本

这种方式是最简单的，一般用于比较简单的逻辑，只需要修改 build.gradle 文件即可。

我们直接在新创建的 Android 项目的 app/build.gradle 文件末尾添加如下代码：

```
class MyPluginExtension {
	// 为插件扩展定义一个字符串类型的变量
    String message = "Hello this is my custom plugin..."
}

// gradle自定义的插件必须继承Plugin接口
class GreetingPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
    	// 创建插件扩展，greeting为插件扩展的名称，可以在gradle文件其他地方使用
        def extension = project.extensions.create('greeting', MyPluginExtension)

        // 任务名为 helloPlugin
        project.task('helloPlugin') {
            doLast {
            	// 插件的任务就是打印message信息
                println extension.message
            }
        }
    }
}

// 使用这个自定义的插件
apply plugin: GreetingPlugin
```

然后 sync 项目，在 Android Studio 右侧的 Gradle 面板中，可以发现 Tasks/others 下面出现了一个 helloPlugin（上面自定义的任务名） 任务，我们双击该任务执行它，会发现控制台中出现如下信息：

```
> Task :app:helloPlugin
Hello this is my custom plugin...
```

这表明我们编写的插件代码正常工作。

其实为了让插件代码跟 app/build.gradle 文件中的其他配置互相独立，可以把插件代码单独用一个 gradle 文件来编写，比如你可以在 app/build.gradle 同级的目录下新建一个 myplugin.gradle 文件，然后将上面的插件代码全部写到其中，再在 app/build.gradle 文件中引用 myplugin.gradle 文件即可，app/build.gradle 中引用的代码如下：

```
// 这一行可以引用外部的gradle文件
apply from: './myplugin.gradle'

// 通过这种配置方式，修改自定义插件中配置的message的值
greeting {
    message = "new message..."
}
```