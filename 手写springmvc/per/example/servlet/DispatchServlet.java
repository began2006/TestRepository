package per.example.servlet;

import per.example.annotation.Autowired_gk;
import per.example.annotation.Controller_gk;
import per.example.annotation.RequestMapping_gk;
import per.example.annotation.Service_gk;
import per.example.exception.GkException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @description:
 * @author: gk
 * @date: 2019/5/30 15:35
 * @since: jdk1.8
 */

public class DispatchServlet extends HttpServlet {

    private List<String> paths = new LinkedList<>();
    private List<String> classNames = new LinkedList<>();
    private Map<String, Object> beans = new HashMap<>();
    private Map<String, String> aliasMap = new HashMap<>();
    private Map<String, MethodHandler> urls = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {

        basePackageScan("per.example");

        try {
            doInstance();

            doAutowired();

            urlMapping();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            //处理请求
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500!! Server Exception");
        }
    }

    public void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String url = request.getRequestURI();

        if (!urls.containsKey(url)) {
            throw new GkException("url not exist. please check.");
        }

        MethodHandler methodHandler = urls.get(url);
        Method method = methodHandler.getMethod();
        Object object = methodHandler.getClassObject();

        Object resultObj = method.invoke(object);
        System.out.println("resultObj = " + resultObj);

        response.getWriter().write(resultObj.toString());
    }

    public void basePackageScan(String basePackage) {

        URL resource = this.getClass().getClassLoader().getResource("/" + basePackage.replaceAll("\\.", "/"));
        String path = resource.getFile();
        System.out.println(" path : " + path);
        getFileList(path);

        String rootPath = this.getClass().getClassLoader().getResource("/").getFile();
        for (String pathTemp : paths) {
            String basePackage1 = pathTemp.substring(rootPath.length()-1).replaceAll("\\\\", "\\.");
            classNames.add(basePackage1.substring(0, basePackage1.indexOf(".class")));
        }
    }

    public void doInstance() throws Exception {
        for (String className : classNames) {
            Class<?> clazz = Class.forName(className);

            // 以下需要实例化
            if (clazz.isAnnotationPresent(Controller_gk.class)) {
                Object object = clazz.newInstance();
                beans.put(className, object);
                aliasMap.put(clazz.getAnnotation(Controller_gk.class).value(), className);
            }
            if (clazz.isAnnotationPresent(Service_gk.class)) {
                Object object = clazz.newInstance();
                Class<?>[] interfaces = clazz.getInterfaces();
                if (interfaces == null) {
                    beans.put(className, object);
                } else {
                    for (Class<?> i : interfaces) {
                        beans.put(i.getName(), object);
                        aliasMap.put(clazz.getAnnotation(Service_gk.class).value(), i.getName());
                    }
                }
            }
        }
    }

    public void doAutowired() throws Exception {
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            String key = entry.getKey();
            Object object = entry.getValue();

            Field[] fields = object.getClass().getDeclaredFields();
            if (fields == null) {
                return;
            }

            for (Field field : fields) {
                if (field.isAnnotationPresent(Autowired_gk.class)) {

                    String beanName = field.getAnnotation(Autowired_gk.class).value();
                    if (!beans.containsKey(beanName)) {
                        if (!aliasMap.containsKey(beanName)) {
                            throw new GkException("beanName not exist. please check again.");
                        }
                        String beanNameTemp = beanName;
                        beanName = aliasMap.get(beanNameTemp);
                    }

                    System.out.println("====   "  + beanName);

                    field.setAccessible(true);
                    field.set(object, beans.get(beanName));
                    field.setAccessible(false);
                }
            }
        }
    }

    public void urlMapping() throws Exception {
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            String key = entry.getKey();
            Object object = entry.getValue();
            Class<?> clazz = object.getClass();

            if (clazz.isAnnotationPresent(Controller_gk.class)) {

                String classUrl = null;
                if (clazz.isAnnotationPresent(RequestMapping_gk.class)) {
                    classUrl = clazz.getAnnotation(RequestMapping_gk.class).value();
                }

                Method[] methods = clazz.getDeclaredMethods();
                if (methods == null) {
                    return;
                }

                for (Method method : methods) {
                    String methodUrl = null;
                    if (method.isAnnotationPresent(RequestMapping_gk.class)) {
                        methodUrl = method.getAnnotation(RequestMapping_gk.class).value();
                        if (classUrl == null) {
                            urls.put(methodUrl, new MethodHandler(object, method));
                        } else {
                            urls.put(classUrl + methodUrl, new MethodHandler(object, method));
                        }
                    }
                }
            }
        }
    }

    private void getFileList(String path) {

        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file1 : files) {
                if (file1.isDirectory()) {
                    getFileList(file1.toString());
                } else {
                    if (file1.toString().contains("example") && !file1.toString().contains("annotation")) {
                        paths.add(file1.toString());
                    }
                }
            }
        }
    }
}
