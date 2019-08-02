package per.example.servlet;

import java.lang.reflect.Method;

/**
 * @description:
 * @author: gk
 * @date: 2019/5/30 17:30
 * @since: jdk1.8
 */

public class MethodHandler {

    private Object classObject;
    private Method method;

    public MethodHandler(Object classObject, Method method) {
        this.classObject = classObject;
        this.method = method;
    }

    public Object getClassObject() {
        return classObject;
    }

    public void setClassObject(Object classObject) {
        this.classObject = classObject;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
