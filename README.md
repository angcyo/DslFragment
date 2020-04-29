# DslFragment

`AndroidX`中, `Fragment`懒加载实现方案,原理就是使用`setMaxLifecycle`控制`Fragment`的生命周期, 以及`Fragment`管理操作助手类`DslFHelper`.

使用`DslFHelper`操作`Fragment`能保证`onResume`和`onPause`方法成对出现, 并且是对用户可见时才触发`onResume`, 对用户不可见时触发`onPause`.

在`ViewPager`中, 请使用`BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT`创建`FragmentStatePagerAdapter`或者`FragmentPagerAdapter`亦可达到相同效果.

![](https://img.shields.io/badge/License-MIT-EA220C) ![](https://img.shields.io/badge/Api-11+-D22730) ![](https://img.shields.io/badge/AndroidX-yes-20803C)
![](https://img.shields.io/badge/Kotlin-yes-B0D922)

![](https://raw.githubusercontent.com/angcyo/DslFragment/master/png/f1.png)

![](https://raw.githubusercontent.com/angcyo/DslFragment/master/png/f2.png)

# 使用说明

完全可以单独只是用`dslFHelper`操作助手, 那么懒加载的处理请在原生的`onResume`和`onPause`回调方法中进行.

如果继承库中`AbsLifecycleFragment`类, 那么懒加载的处理请在`onFragmentShow`和`onFragmentHide`回调方法中进行.

如果使用原生或者第三方的`Fragment`时, 请实现`IFragment`接口, 这样在使用`dslFHelper`时能保证功能的正常进行.


在`Activity`和`Fragment`中均可以直接使用扩展方法`dslFHelper`.

在`Fragment`中有`dslChildFHelper`扩展方法.

> 注意:`Fragment`容器的id请使用`R.id.fragment_container`, 否则在操作`DslFHelper`需要指定`containerViewId`变量

## 显示`Fragment`

在无特殊说明的情况下, 均支持批量操作`Fragment`, 比如可以同时显示多个`Fragment`和移除多个`Fragment`.

```kotlin
dslFHelper {
    show(TestFragment2::class.java)
}

dslFHelper {
    show(TestFragment2())
}

dslFHelper {
    //优先从缓存池中获取目标Fragment
    restore(TestFragment2::class.java) 
}

```

## 移除`Fragment`

```kotlin
dslFHelper {
    //移除指定的fragment
    remove(this@TestFragment2)
}
    
dslFHelper {
    //保留tag对应的fragment, 其他全部移除
    keep(MainFragment::class.java)
}

dslFHelper {
    //移除全部在FragmentManager中的fragment
    removeAll()
}
```

## 动画处理

可以通过一下变量配置动画,支持`补间动画`和`属性动画`:

```kotlin
 @AnimatorRes
 @AnimRes
 var showEnterAnimRes: Int
 @AnimatorRes
 @AnimRes
 var showExitAnimRes: Int
 @AnimatorRes
 @AnimRes
 var removeEnterAnimRes: Int
 @AnimatorRes
 @AnimRes
 var removeExitAnimRes: Int
```

`补间动画`不需要额外的处理, 即可达到效果.
如果是`属性动画`, 并且需要100%宽度, 100%高度这样的数据, 就需要额外的处理方式了.

如果是继承`AbsLifecycleFragment`, 则不需要额外处理.

如果不是继承的`AbsLifecycleFragment`, 那么只需要在`Fragment`中的`onCreateAnimator`返回`return FragmentAnimator.loadAnimator(context, nextAnim)`即可.

## 关于`返回`按键的处理

## 1.
`Activity`可以直接继承库中`BaseAppCompatActivity`即可.

## 2.
或者在`Activity`的`onBackPressed`方法中, 加入以下代码:
```
dslFHelper {
    if (back()) {
        //finish activity
    }
}
```

实现了`IFragment`接口的`Fragment`, 会在`back()`处理的时候, 收到`onBackPressed`回调, 可以通过返回`false`拦截此次`back`操作.

在`Fragment`内可以使用`back`扩展方法, 进行回退操作.

> 参数的传递, 均使用原生默认的`arguments = Bundle()`方式.

# 使用`JitPack`的方式, 引入库.

## 根目录中的 `build.gradle`

```kotlin
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

## APP目录中的 `build.gradle`

```kotlin
dependencies {
    implementation 'com.github.angcyo:DslFragment:1.0.1'
}
```

---
**群内有`各(pian)种(ni)各(jin)样(qun)`的大佬,等你来撩.**

# 联系作者

[点此QQ对话](http://wpa.qq.com/msgrd?v=3&uin=664738095&site=qq&menu=yes)  `该死的空格`    [点此快速加群](https://shang.qq.com/wpa/qunwpa?idkey=cbcf9a42faf2fe730b51004d33ac70863617e6999fce7daf43231f3cf2997460)

[开源地址](https://github.com/angcyo/DslAdapter)

![](https://gitee.com/angcyo/res/raw/master/code/all_in1.jpg)

![](https://gitee.com/angcyo/res/raw/master/code/all_in2.jpg)
