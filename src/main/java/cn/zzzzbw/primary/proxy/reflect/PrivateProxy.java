package cn.zzzzbw.primary.proxy.reflect;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author by zzzzbw
 * @since 2020/09/15 9:24
 */
public class PrivateProxy {

    private static final String proxyClassNamePrefix = "$Proxy";
    private static final AtomicLong nextUniqueNumber = new AtomicLong();

    public static Object newProxyInstance(ClassLoader loader, Class<?>[] interfaces, PrivateProxyInvocationHandler h) {
        try {
            // 1 生成java源码
            String packageName = PrivateProxy.class.getPackage().getName();
            long number = nextUniqueNumber.getAndAdd(1);
            String clazzName = proxyClassNamePrefix + number;
            String proxyName = packageName + "." + clazzName;
            String src = PrivateProxyGenerator.generateProxyClass(proxyName, interfaces, h);

            // 2 讲源码输出到java文件中
            String filePath = PrivateProxy.class.getResource("/").getPath();
            String clzFilePath = filePath + packageName.replace(".", "/") + "/" + clazzName + ".java";
            File f = new File(clzFilePath);
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            try (FileWriter fw = new FileWriter(f)) {
                fw.write(src);
                fw.flush();
            }

            //3、将java文件编译成class文件
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager manage = compiler.getStandardFileManager(null, null, null);
            Iterable<? extends JavaFileObject> iterable = manage.getJavaFileObjects(f);
            JavaCompiler.CompilationTask task = compiler.getTask(null, manage, null, null, null, iterable);
            task.call();
            manage.close();

            f.delete();

            //4、将class加载进jvm
            Class<?> proxyClass = loader.loadClass(proxyName);


            Constructor<?> constructor = proxyClass.getConstructor(PrivateProxyInvocationHandler.class);
            return constructor.newInstance(h);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
