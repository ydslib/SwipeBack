# SwipeBack
滑动退出，支持四个方向上的滑动退出

本库大部分源码参考自https://github.com/goweii/SwipeBack, 里面有些许改动，主要是为了方便自己使用及根据自己需求修改内容



**Step 1.** Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```java
allprojects {
    repositories {
	...
	maven { url 'https://www.jitpack.io' }
    }
}
```
**Step 2.** Add the dependency
```java
dependencies {
    implementation 'com.github.ydslib:SwipeBack:1.0.0'
}
```
