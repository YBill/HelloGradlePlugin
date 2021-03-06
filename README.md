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


#### 下面分别实现这三种编写 Gradle 插件的流程。


##### 1、构建脚本

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

##### 2、buildSrc 项目

buildSrc 编写 gradle 插件项目主要也是用在当前项目中，不能被外部的项目引用，它的创建有一套固定的流程，步骤如下：

（1）在项目根目录下新建一个 buildSrc 目录，然后点击 Android Studio 的 Make Project 按钮编译项目，IDE 会自动在 buildSrc 目录下生成 .gradle 和 build 文件

（2）在 buildSrc 目录下新建 build.gradle 文件并加入如下代码：

```
apply plugin: 'java-library'

sourceSets {
    main {
        java{
            srcDir 'src/main/java'
        }
        resources {
            srcDir 'src/main/resources'
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
```
(3) 在 buildSrc 目录下创建 src 目录，并在 src 目录下分别创建 main/java 和 main/resources 目录

(4) 在 src/main/java 目录下编写插件代码（注意这里插件是个java类），比如这里我们创建一个简单的插件类，代码如下：

```
package com.bill.inner.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

class MyPlugin implements Plugin<Project> {

    @Override
    public void apply(Project target) {
        System.out.println("Hello this is a inner gradle plugin...");
    }
}
```

（5）配置插件。要配置插件，需要在 src/main/resources 目录下再创建 META-INF/gradle-plugins 目录，并在该目录中添加一个文件，名字为`使用时插件名+.properties`，如 com.bill.inner.plugins.properties，该文件内容如下：

```
implementation-class=com.bill.inner.plugin.MyPlugin // 第4步中插件全路径
```

以上所有步骤都做完之后就完成了，使用时在 module 的 build.gradle 中添加 `apply plugin: 'com.bill.inner.plugin'` 文件即可

点击 Android Studio 的 Make Project 按钮编译项目，在 Build 下打印出如下日志

```
> Configure project :app
Hello this is a inner gradle plugin...
```

##### 3、独立项目

如果要使我们编写的 gradle 插件被外部项目所引用，比如每个 Android Studio 创建的项目都依赖了 'com.android.application' 这个插件，那么我们就需要使用这种独立项目来完成 gradle 插件的开发了，开发步骤如下：

（1）在 Android 项目上右键，选择 New - Module - Java or Kotlin Library 创建一个 Java library，这里我们取名为 myplugin

（2）在该 module 的 build.gradle 文件中编写如下代码：

```
apply plugin: 'groovy'
apply plugin: 'maven-publish'

repositories {
    mavenLocal()
}

dependencies {
    implementation gradleApi()
}

afterEvaluate {
    publishing {
        publications {
            publish2Local(MavenPublication) {
                groupId = "com.bill.library"
                artifactId = "mylib"
                version = "1.0.0"

                from components.java

            }

        }

        repositories {
            maven {
                url = "../repo"
            }
        }
    }
}
```

（3）同第二种实现 gradle 插件的方式一样，在 library 的根目录下创建 src 目录，并在 src 目录下分别创建 main/groovy（可以是groovy/java/kotlin,对应的插件文件也对应.groovy/.java/.kt） 和 main/resources 目录

（4）在 src/main/java 目录下编写插件代码，这里测试用的代码如下：

```
package com.bill.myplugin

import org.gradle.api.Plugin;
import org.gradle.api.Project;

class MyPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        System.out.println("Hello this is a single gradle plugin...");
    }
}
```

（5）在 src/main/resources/META-INF/gradle-plugins 目录下创建文件，文件名为 com.bill.myplugin.properties，文件内容为：

```
implementation-class=com.bill.myplugin.MyPlugin
```

（6）sync 项目，在 Android Studio 右侧的 Gradle 面板中，我们会看到该插件对应的任务，双击 publish，AS 会自动打包并将代码发布到 repo 仓库（上面配置的url）


以上所有步骤都做完之后就完成了，下面是使用该插件：

（1）在项目的 build.gradle 中添加：

```
buildscript {
    repositories {
        google()
        mavenCentral()
        // 1、这里的地址填你生成的repo目录地址
        maven {
            url "$rootDir\\repo"
        }
    }
    dependencies {
        classpath "com.android.tools.build:gradle:4.0.0"

        classpath "com.example.plugin:plugin:1.0.0" // 2、　添加依赖

    }
}

```

（2）在 app module 的 build.gradle 中引用该插件：

```
apply plugin: 'com.bill.myplugin'
```

然后编译项目，可以看到该插件输出的日志信息：

```
> Configure project :app
Hello this is a single gradle plugin...
```



