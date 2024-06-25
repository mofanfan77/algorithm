package utility.annotation

/**
 *
 * ignored  标注注解 ， 当 class 或者 function  被标注后 后续封装的api 将会忽略这部分代码
 * 注意  :
 *    <li> 该注解的声明周期只会在于源代码阶段 </li>
 *   <li> 无法使用APT类型工具获取</li>
 *
 * Create by yang yx at  2024/05/20 <br/>
 */
@Retention(AnnotationRetention.SOURCE)
@Target(allowedTargets = [AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER])
annotation class Ignored
