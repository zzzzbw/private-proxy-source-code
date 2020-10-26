package cn.zzzzbw.primary.proxy.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author by zzzzbw
 * @since 2020/09/17 14:30
 */
public class PrivateProxyGenerator {

    public static final String WRAP = "\r\n";

    public static final String SPACE = " ";

    public static final String SEMICOLON = ";";

    public static final String PUBLIC = "public";

    public static final String PRIVATE = "private";

    private final String className;

    /**
     * 代理方法
     */
    private final PrivateProxyInvocationHandler h;

    /**
     * 代理接口
     */
    private final Class<?>[] interfaces;

    /**
     * 代理方法
     * key: 接口类
     * value: 接口声明的方法列表
     */
    private final Map<Class<?>, List<Method>> proxyMethods = new HashMap<>();
    /**
     * 代理的私有方法
     */
    private final List<Method> privateMethods = new ArrayList<>();

    private PrivateProxyGenerator(String proxyName, Class<?>[] interfaces, PrivateProxyInvocationHandler h) {
        this.className = proxyName;
        this.interfaces = interfaces;
        this.h = h;
    }

    public static String generateProxyClass(final String proxyName, Class<?>[] interfaces, PrivateProxyInvocationHandler h) {
        PrivateProxyGenerator generator = new PrivateProxyGenerator(proxyName, interfaces, h);
        return generator.generateClassSrc();
    }

    private String generateClassSrc() {
        // 1. 添加equal、hashcode、toString方法
        // 这里省略

        // 2. 添加interface中的方法
        for (Class<?> interfaceClz : interfaces) {
            // TODO 这里就不考虑多个interfaces含有相同method的情况了
            Method[] methods = interfaceClz.getMethods();
            this.proxyMethods.put(interfaceClz, Arrays.asList(methods));
        }


        // 3. 添加代理类中的私有方法
        // TODO 这是新增的
        Object subject = h.getSubject();
        Method[] declaredMethods = subject.getClass().getDeclaredMethods();
        List<Method> privateMethods = Arrays.stream(declaredMethods)
                .filter(method -> method.getModifiers() == Modifier.PRIVATE)
                .collect(Collectors.toList());

        this.privateMethods.addAll(privateMethods);


        // 4. 校验方法的签名等@see sun.misc.ProxyGenerator.checkReturnTypes
        // 这里省略


        // 5. 添加类里的字段信息和方法数据
        // 如静态方法、构造方法、字段等
        // TODO 这里省略, 在编写java字符串(步骤7)时直接写入

        // 6. 校验一下方法长度、字段长度等
        // 这里省略

        // 7. 把刚才添加的数据真正写到class文件里
        // TODO 这里我们根据逻辑写成java字符串
        return writeJavaSrc();
    }


    /**
     * 将代理类数据写成对应的java文件
     *
     * @return
     */
    private String writeJavaSrc() {
        StringBuffer sb = new StringBuffer();

        int packageIndex = this.className.lastIndexOf(".");
        String packageName = this.className.substring(0, packageIndex);
        String clzName = this.className.substring(packageIndex + 1);

        // package信息
        sb.append("package").append(SPACE).append(packageName).append(SEMICOLON).append(WRAP);


        // class 信息, interface接口
        sb.append(PUBLIC).append(SPACE).append("class").append(SPACE).append(clzName).append(SPACE);
        sb.append("implements").append(SPACE);

        String interfaceNameList = Arrays.stream(this.interfaces).map(Class::getTypeName).collect(Collectors.joining(","));
        sb.append(interfaceNameList);

        sb.append(SPACE).append("{").append(WRAP);


        // 必须要的属性和构造函数
        /**
         * private PrivateProxyInvocationHandler h;
         */
        sb.append(PRIVATE).append(SPACE).append(PrivateProxyInvocationHandler.class.getName()).append(SPACE).append("h;").append(WRAP);

        /**
         *  public $Proxy0(PrivateProxyInvocationHandler h) {
         *      this.h = h;
         * }
         */
        sb.append(PUBLIC).append(SPACE).append(clzName).append("(")
                .append(PrivateProxyInvocationHandler.class.getName()).append(SPACE).append("h").append("){").append(WRAP)
                .append("this.h = h;").append(WRAP)
                .append("}");


        // 代理public方法
        this.proxyMethods.forEach((interfaceClz, methods) -> {
            for (Method proxyMethod : methods) {
                writeProxyMethod(sb, interfaceClz, proxyMethod, PUBLIC);
            }
        });


        // 代理private方法
        for (Method proxyMethod : this.privateMethods) {
            writeProxyMethod(sb, null, proxyMethod, PRIVATE);
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * 编写代理方法数据
     *
     * @param sb           StringBuffer
     * @param interfaceClz 接口类，如果代理私有方法则不用传
     * @param proxyMethod  目标代理方法
     * @param accessFlag   方法类型
     */
    private void writeProxyMethod(StringBuffer sb, Class<?> interfaceClz, Method proxyMethod, String accessFlag) {
        // 1. 编写方法的声明，例：
        // public void hello(java.lang.String var0)
        sb.append(accessFlag)
                .append(SPACE)
                // 返回类
                .append(proxyMethod.getReturnType().getTypeName()).append(SPACE)
                .append(proxyMethod.getName()).append("(");

        // 参数类
        Class<?>[] parameterTypes = proxyMethod.getParameterTypes();
        // 参数类名
        List<String> argClassNames = new ArrayList<>();
        // 参数名
        List<String> args = new ArrayList<>();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            argClassNames.add(parameterType.getTypeName());
            args.add("var" + i);
        }
        // 写入参数的声明
        for (int i = 0; i < args.size(); i++) {
            sb.append(argClassNames.get(i)).append(SPACE).append(args.get(i)).append(",");
        }
        if (parameterTypes.length > 0) {
            //去掉最后一个逗号
            sb.replace(sb.length() - 1, sb.length(), "");
        }

        sb.append(")").append("{").append(WRAP);

        // 如果是public方法，则编写的代理方法逻辑大致如下
        /**
         * try {
         *  Method m = HelloService.class.getMethod("hello", String.class, Integer.class);
         *  return this.h.invoke(this, proxyMethod, new Object[]{var0, var1...});
         * } catch (Throwable e) {
         *  throw new RuntimeException(e);
         * }
         */

        // 如果是private方法，则编写的代理方法逻辑大致如下
        /**
         * try {
         *  Method m = h.getSubject().getClass().getDeclaredMethod("hello", String.class, Integer.class);
         *  m.setAccessible(true);
         *  return this.h.invoke(this, proxyMethod, new Object[]{var0, var1...});
         * } catch (Throwable e) {
         *  throw new RuntimeException(e);
         * }
         */

        // 2. try
        sb.append("try{").append(WRAP);

        // 3. 编写获取目标代理方法的功能
        sb.append(Method.class.getTypeName()).append(SPACE).append("m = ");
        if (PUBLIC.equals(accessFlag)) {
            // 3.1 public方法的代理, 通过接口获取实例方法。例:
            // java.lang.reflect.Method m = HelloService.class.getMethod("hello", String.class, Integer.class);
            sb.append(interfaceClz.getTypeName()).append(".class")
                    .append(".getMethod(").append("\"").append(proxyMethod.getName()).append("\"").append(",").append(SPACE);
        } else {
            // 3.2 private方法的代理, 通过目标代理类实例获取方法。例:
            // java.lang.reflect.Method m = h.getSubject().getClass().getDeclaredMethod("hello", String.class, Integer.class);
            sb.append("h.getSubject().getClass().getDeclaredMethod(").append("\"").append(proxyMethod.getName()).append("\"").append(",").append(SPACE);
        }


        argClassNames.forEach(name -> sb.append(name).append(".class").append(","));
        if (parameterTypes.length > 0) {
            //去掉最后一个逗号
            sb.replace(sb.length() - 1, sb.length(), "");
        }
        sb.append(");").append(WRAP);

        if (!PUBLIC.equals(accessFlag)) {
            // 3.3 不是public方法，设置访问权限
            sb.append("m.setAccessible(true);").append(WRAP);
        }

        // 4. InvocationHandler中调用代理方法逻辑, 例：
        // return this.h.invoke(this, m, new Object[]{var0});
        if (!proxyMethod.getReturnType().equals(Void.class) && !proxyMethod.getReturnType().equals(void.class)) {
            // 有返回值则返回且强转
            sb.append("return").append(SPACE).append("(").append(proxyMethod.getReturnType().getName()).append(")");
        }

        String argsList = String.join(",", args);
        sb.append("this.h.invoke(this, m, new Object[]{").append(argsList).append("});");

        // 5. catch
        sb.append("} catch (Throwable e) {").append(WRAP);
        sb.append("throw new RuntimeException(e);").append(WRAP);
        sb.append("}");

        sb.append("}").append(WRAP);
    }

}
