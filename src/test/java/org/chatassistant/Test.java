package org.chatassistant;

import org.reflections.Reflections;
import org.chatassistant.healthcheck.HealthCheck;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class Test {
    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
        final Reflections reflections = new Reflections("org.scheduler");
        final Set<Class<? extends HealthCheck>> healthChecks = reflections.getSubTypesOf(HealthCheck.class);

        for(final Class<? extends HealthCheck> healthCheck : healthChecks){
            try{
                for(Method method : healthCheck.getDeclaredMethods()){
                    System.out.println(method.getName());
                }
                healthCheck.getMethods()[0].invoke(null, new String[1]);
            } catch(Exception e){
                final String className = healthCheck.getSimpleName();
                System.out.println("Error with " + className.substring(0, className.indexOf("HealthCheck")));
                e.printStackTrace();
            }
        }

    }
}
