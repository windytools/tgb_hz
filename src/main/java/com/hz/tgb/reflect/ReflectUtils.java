package com.hz.tgb.reflect;

import java.lang.reflect.*;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * ReflectUtils
 *
 * 拷贝：com.alibaba.dubbo.common.utils.ReflectUtils
 *
 * Created by hezhao on 2018-07-02 19:52
 */
public final class ReflectUtils {

    /**
     * void(V).
     */
    public static final char JVM_VOID = 'V';

    /**
     * boolean(Z).
     */
    public static final char JVM_BOOLEAN = 'Z';

    /**
     * byte(B).
     */
    public static final char JVM_BYTE = 'B';

    /**
     * char(C).
     */
    public static final char JVM_CHAR = 'C';

    /**
     * double(D).
     */
    public static final char JVM_DOUBLE = 'D';

    /**
     * float(F).
     */
    public static final char JVM_FLOAT = 'F';

    /**
     * int(I).
     */
    public static final char JVM_INT = 'I';

    /**
     * long(J).
     */
    public static final char JVM_LONG = 'J';

    /**
     * short(S).
     */
    public static final char JVM_SHORT = 'S';

    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

    public static final String JAVA_IDENT_REGEX = "(?:[_$a-zA-Z][_$a-zA-Z0-9]*)";

    public static final String JAVA_NAME_REGEX = "(?:" + JAVA_IDENT_REGEX + "(?:\\." + JAVA_IDENT_REGEX + ")*)";

    public static final String CLASS_DESC = "(?:L" + JAVA_IDENT_REGEX   + "(?:\\/" + JAVA_IDENT_REGEX + ")*;)";

    public static final String ARRAY_DESC  = "(?:\\[+(?:(?:[VZBCDFIJS])|" + CLASS_DESC + "))";

    public static final String DESC_REGEX = "(?:(?:[VZBCDFIJS])|" + CLASS_DESC + "|" + ARRAY_DESC + ")";

    public static final Pattern DESC_PATTERN = Pattern.compile(DESC_REGEX);

    public static final String METHOD_DESC_REGEX = "(?:("+JAVA_IDENT_REGEX+")?\\(("+DESC_REGEX+"*)\\)("+DESC_REGEX+")?)";

    public static final Pattern METHOD_DESC_PATTERN = Pattern.compile(METHOD_DESC_REGEX);

    public static final Pattern GETTER_METHOD_DESC_PATTERN = Pattern.compile("get([A-Z][_a-zA-Z0-9]*)\\(\\)(" + DESC_REGEX + ")");

    public static final Pattern SETTER_METHOD_DESC_PATTERN = Pattern.compile("set([A-Z][_a-zA-Z0-9]*)\\((" + DESC_REGEX + ")\\)V");

    public static final Pattern IS_HAS_CAN_METHOD_DESC_PATTERN = Pattern.compile("(?:is|has|can)([A-Z][_a-zA-Z0-9]*)\\(\\)Z");

    private static final ConcurrentMap<String, Class<?>> DESC_CLASS_CACHE = new ConcurrentHashMap<String, Class<?>>();

    private static final ConcurrentMap<String, Class<?>>  NAME_CLASS_CACHE = new ConcurrentHashMap<String, Class<?>>();

    private static final ConcurrentMap<String, Method>  Signature_METHODS_CACHE = new ConcurrentHashMap<String, Method>();

    public static boolean isPrimitives(Class<?> cls) {
        if (cls.isArray()) {
            return isPrimitive(cls.getComponentType());
        }
        return isPrimitive(cls);
    }

    public static boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive() || cls == String.class || cls == Boolean.class || cls == Character.class
                || Number.class.isAssignableFrom(cls) || Date.class.isAssignableFrom(cls);
    }

    public static Class<?> getBoxedClass(Class<?> c) {
        if( c == int.class )
            c = Integer.class;
        else if( c == boolean.class )
            c = Boolean.class;
        else  if( c == long.class )
            c = Long.class;
        else if( c == float.class )
            c = Float.class;
        else if( c == double.class )
            c = Double.class;
        else if( c == char.class )
            c = Character.class;
        else if( c == byte.class )
            c = Byte.class;
        else if( c == short.class )
            c = Short.class;
        return c;
    }

    /**
     * is compatible.
     *
     * @param c class.
     * @param o instance.
     * @return compatible or not.
     */
    public static boolean isCompatible(Class<?> c, Object o)
    {
        boolean pt = c.isPrimitive();
        if( o == null )
            return !pt;

        if( pt )
        {
            if( c == int.class )
                c = Integer.class;
            else if( c == boolean.class )
                c = Boolean.class;
            else  if( c == long.class )
                c = Long.class;
            else if( c == float.class )
                c = Float.class;
            else if( c == double.class )
                c = Double.class;
            else if( c == char.class )
                c = Character.class;
            else if( c == byte.class )
                c = Byte.class;
            else if( c == short.class )
                c = Short.class;
        }
        if( c == o.getClass() )
            return true;
        return c.isInstance(o);
    }

    /**
     * is compatible.
     *
     * @param cs class array.
     * @param os object array.
     * @return compatible or not.
     */
    public static boolean isCompatible(Class<?>[] cs, Object[] os)
    {
        int len = cs.length;
        if( len != os.length ) return false;
        if( len == 0 ) return true;
        for(int i=0;i<len;i++)
            if( !isCompatible(cs[i], os[i]) ) return false;
        return true;
    }

    public static String getCodeBase(Class<?> cls) {
        if (cls == null)
            return null;
        ProtectionDomain domain = cls.getProtectionDomain();
        if (domain == null)
            return null;
        CodeSource source = domain.getCodeSource();
        if (source == null)
            return null;
        URL location = source.getLocation();
        if (location == null)
            return null;
        return location.getFile();
    }

    /**
     * get name.
     * java.lang.Object[][].class => "java.lang.Object[][]"
     *
     * @param c class.
     * @return name.
     */
    public static String getName(Class<?> c)
    {
        if( c.isArray() )
        {
            StringBuilder sb = new StringBuilder();
            do
            {
                sb.append("[]");
                c = c.getComponentType();
            }
            while( c.isArray() );

            return c.getName() + sb.toString();
        }
        return c.getName();
    }


    public static Class<?> getGenericClass(Class<?> cls) {
        return getGenericClass(cls, 0);
    }

    public static Class<?> getGenericClass(Class<?> cls, int i) {
        try {
            ParameterizedType parameterizedType = ((ParameterizedType) cls.getGenericInterfaces()[0]);
            Object genericClass = parameterizedType.getActualTypeArguments()[i];
            if (genericClass instanceof ParameterizedType) { // 处理多级泛型
                return (Class<?>) ((ParameterizedType) genericClass).getRawType();
            } else if (genericClass instanceof GenericArrayType) { // 处理数组泛型
                return (Class<?>) ((GenericArrayType) genericClass).getGenericComponentType();
            } else {
                return (Class<?>) genericClass;
            }
        } catch (Throwable e) {
            throw new IllegalArgumentException(cls.getName()
                    + " generic type undefined!", e);
        }
    }


    /**
     * get method name.
     * "void do(int)", "void do()", "int do(java.lang.String,boolean)"
     *
     * @param m method.
     * @return name.
     */
    public static String getName(final Method m)
    {
        StringBuilder ret = new StringBuilder();
        ret.append(getName(m.getReturnType())).append(' ');
        ret.append(m.getName()).append('(');
        Class<?>[] parameterTypes = m.getParameterTypes();
        for(int i=0;i<parameterTypes.length;i++)
        {
            if( i > 0 )
                ret.append(',');
            ret.append(getName(parameterTypes[i]));
        }
        ret.append(')');
        return ret.toString();
    }

    public static String getSignature(String methodName, Class<?>[] parameterTypes) {
        StringBuilder sb = new StringBuilder(methodName);
        sb.append("(");
        if (parameterTypes != null && parameterTypes.length > 0) {
            boolean first = true;
            for (Class<?> type : parameterTypes) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                sb.append(type.getName());
            }
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * get constructor name.
     * "()", "(java.lang.String,int)"
     *
     * @param c constructor.
     * @return name.
     */
    public static String getName(final Constructor<?> c)
    {
        StringBuilder ret = new StringBuilder("(");
        Class<?>[] parameterTypes = c.getParameterTypes();
        for(int i=0;i<parameterTypes.length;i++)
        {
            if( i > 0 )
                ret.append(',');
            ret.append(getName(parameterTypes[i]));
        }
        ret.append(')');
        return ret.toString();
    }

    /**
     * get class desc.
     * boolean[].class => "[Z"
     * Object.class => "Ljava/lang/Object;"
     *
     * @param c class.
     * @return desc.
     */
    public static String getDesc(Class<?> c)
    {
        StringBuilder ret = new StringBuilder();

        while( c.isArray() )
        {
            ret.append('[');
            c = c.getComponentType();
        }

        if( c.isPrimitive() )
        {
            String t = c.getName();
            if( "void".equals(t) ) ret.append(JVM_VOID);
            else if( "boolean".equals(t) ) ret.append(JVM_BOOLEAN);
            else if( "byte".equals(t) ) ret.append(JVM_BYTE);
            else if( "char".equals(t) ) ret.append(JVM_CHAR);
            else if( "double".equals(t) ) ret.append(JVM_DOUBLE);
            else if( "float".equals(t) ) ret.append(JVM_FLOAT);
            else if( "int".equals(t) ) ret.append(JVM_INT);
            else if( "long".equals(t) ) ret.append(JVM_LONG);
            else if( "short".equals(t) ) ret.append(JVM_SHORT);
        }
        else
        {
            ret.append('L');
            ret.append(c.getName().replace('.', '/'));
            ret.append(';');
        }
        return ret.toString();
    }

    /**
     * get class array desc.
     * [int.class, boolean[].class, Object.class] => "I[ZLjava/lang/Object;"
     *
     * @param cs class array.
     * @return desc.
     */
    public static String getDesc(final Class<?>[] cs)
    {
        if( cs.length == 0 )
            return "";

        StringBuilder sb = new StringBuilder(64);
        for( Class<?> c : cs )
            sb.append(getDesc(c));
        return sb.toString();
    }

    /**
     * get method desc.
     * int do(int arg1) => "do(I)I"
     * void do(String arg1,boolean arg2) => "do(Ljava/lang/String;Z)V"
     *
     * @param m method.
     * @return desc.
     */
    public static String getDesc(final Method m)
    {
        StringBuilder ret = new StringBuilder(m.getName()).append('(');
        Class<?>[] parameterTypes = m.getParameterTypes();
        for(int i=0;i<parameterTypes.length;i++)
            ret.append(getDesc(parameterTypes[i]));
        ret.append(')').append(getDesc(m.getReturnType()));
        return ret.toString();
    }

    /**
     * get constructor desc.
     * "()V", "(Ljava/lang/String;I)V"
     *
     * @param c constructor.
     * @return desc
     */
    public static String getDesc(final Constructor<?> c)
    {
        StringBuilder ret = new StringBuilder("(");
        Class<?>[] parameterTypes = c.getParameterTypes();
        for(int i=0;i<parameterTypes.length;i++)
            ret.append(getDesc(parameterTypes[i]));
        ret.append(')').append('V');
        return ret.toString();
    }

    /**
     * get method desc.
     * "(I)I", "()V", "(Ljava/lang/String;Z)V"
     *
     * @param m method.
     * @return desc.
     */
    public static String getDescWithoutMethodName(Method m)
    {
        StringBuilder ret = new StringBuilder();
        ret.append('(');
        Class<?>[] parameterTypes = m.getParameterTypes();
        for(int i=0;i<parameterTypes.length;i++)
            ret.append(getDesc(parameterTypes[i]));
        ret.append(')').append(getDesc(m.getReturnType()));
        return ret.toString();
    }

    /**
     * name to desc.
     * java.util.Map[][] => "[[Ljava/util/Map;"
     *
     * @param name name.
     * @return desc.
     */
    public static String name2desc(String name)
    {
        StringBuilder sb = new StringBuilder();
        int c = 0,index = name.indexOf('[');
        if( index > 0 )
        {
            c = ( name.length() - index ) / 2;
            name = name.substring(0,index);
        }
        while( c-- > 0 ) sb.append("[");
        if( "void".equals(name) ) sb.append(JVM_VOID);
        else if( "boolean".equals(name) ) sb.append(JVM_BOOLEAN);
        else if( "byte".equals(name) ) sb.append(JVM_BYTE);
        else if( "char".equals(name) ) sb.append(JVM_CHAR);
        else if( "double".equals(name) ) sb.append(JVM_DOUBLE);
        else if( "float".equals(name) ) sb.append(JVM_FLOAT);
        else if( "int".equals(name) ) sb.append(JVM_INT);
        else if( "long".equals(name) ) sb.append(JVM_LONG);
        else if( "short".equals(name) ) sb.append(JVM_SHORT);
        else sb.append('L').append(name.replace('.', '/')).append(';');
        return sb.toString();
    }

    /**
     * desc to name.
     * "[[I" => "int[][]"
     *
     * @param desc desc.
     * @return name.
     */
    public static String desc2name(String desc)
    {
        StringBuilder sb = new StringBuilder();
        int c = desc.lastIndexOf('[') + 1;
        if( desc.length() == c+1 )
        {
            switch( desc.charAt(c) )
            {
                case JVM_VOID: { sb.append("void"); break; }
                case JVM_BOOLEAN: { sb.append("boolean"); break; }
                case JVM_BYTE: { sb.append("byte"); break; }
                case JVM_CHAR: { sb.append("char"); break; }
                case JVM_DOUBLE: { sb.append("double"); break; }
                case JVM_FLOAT: { sb.append("float"); break; }
                case JVM_INT: { sb.append("int"); break; }
                case JVM_LONG: { sb.append("long"); break; }
                case JVM_SHORT: { sb.append("short"); break; }
                default:
                    throw new RuntimeException();
            }
        }
        else
        {
            sb.append(desc.substring(c+1, desc.length()-1).replace('/','.'));
        }
        while( c-- > 0 ) sb.append("[]");
        return sb.toString();
    }

    /**
     * 根据方法签名从类中找出方法。
     *
     * @param clazz 查找的类。
     * @param methodName 方法签名，形如method1(int, String)。也允许只给方法名不参数只有方法名，形如method2。
     * @return 返回查找到的方法。
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     * @throws IllegalStateException 给定的方法签名找到多个方法（方法签名中没有指定参数，又有有重载的方法的情况）
     */
    public static Method findMethodByMethodSignature(Class<?> clazz, String methodName, String[] parameterTypes)
            throws NoSuchMethodException, ClassNotFoundException {
        String signature = methodName;
        if(parameterTypes != null && parameterTypes.length > 0){
            signature = methodName + join(parameterTypes);
        }
        Method method = Signature_METHODS_CACHE.get(signature);
        if(method != null){
            return method;
        }
        if (parameterTypes == null) {
            List<Method> finded = new ArrayList<Method>();
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(methodName)) {
                    finded.add(m);
                }
            }
            if (finded.isEmpty()) {
                throw new NoSuchMethodException("No such method " + methodName + " in class " + clazz);
            }
            if(finded.size() > 1) {
                String msg = String.format("Not unique method for method name(%s) in class(%s), find %d methods.",
                        methodName, clazz.getName(), finded.size());
                throw new IllegalStateException(msg);
            }
            method = finded.get(0);
        } else {
            Class<?>[] types = new Class<?>[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i ++) {
                types[i] = com.alibaba.dubbo.common.utils.ReflectUtils.name2class(parameterTypes[i]);
            }
            method = clazz.getMethod(methodName, types);

        }
        Signature_METHODS_CACHE.put(signature, method);
        return method;
    }

    private static String join(String[] array) {
        if( array.length == 0 ) return "";
        StringBuilder sb = new StringBuilder();
        for( String s : array )
            sb.append(s);
        return sb.toString();
    }

    public static Method findMethodByMethodName(Class<?> clazz, String methodName)
            throws NoSuchMethodException, ClassNotFoundException {
        return findMethodByMethodSignature(clazz, methodName, null);
    }

    public static Constructor<?> findConstructor(Class<?> clazz, Class<?> paramType) throws NoSuchMethodException {
        Constructor<?> targetConstructor;
        try {
            targetConstructor = clazz.getConstructor(new Class<?>[] {paramType});
        } catch (NoSuchMethodException e) {
            targetConstructor = null;
            Constructor<?>[] constructors = clazz.getConstructors();
            for (Constructor<?> constructor : constructors) {
                if (Modifier.isPublic(constructor.getModifiers())
                        && constructor.getParameterTypes().length == 1
                        && constructor.getParameterTypes()[0].isAssignableFrom(paramType)) {
                    targetConstructor = constructor;
                    break;
                }
            }
            if (targetConstructor == null) {
                throw e;
            }
        }
        return targetConstructor;
    }

    /**
     * 检查对象是否是指定接口的实现。
     * <p>
     * 不会触发到指定接口的{@link Class}，所以如果ClassLoader中没有指定接口类时，也不会出错。
     *
     * @param obj 要检查的对象
     * @param interfaceClazzName 指定的接口名
     * @return 返回{@code true}，如果对象实现了指定接口；否则返回{@code false}。
     */
    public static boolean isInstance(Object obj, String interfaceClazzName) {
        for (Class<?> clazz = obj.getClass();
             clazz != null && !clazz.equals(Object.class);
             clazz = clazz.getSuperclass()) {
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> itf : interfaces) {
                if (itf.getName().equals(interfaceClazzName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Object getEmptyObject(Class<?> returnType) {
        return getEmptyObject(returnType, new HashMap<Class<?>, Object>(), 0);
    }

    private static Object getEmptyObject(Class<?> returnType, Map<Class<?>, Object> emptyInstances, int level) {
        if (level > 2)
            return null;
        if (returnType == null) {
            return null;
        } else if (returnType == boolean.class || returnType == Boolean.class) {
            return false;
        } else if (returnType == char.class || returnType == Character.class) {
            return '\0';
        } else if (returnType == byte.class || returnType == Byte.class) {
            return (byte)0;
        } else if (returnType == short.class || returnType == Short.class) {
            return (short)0;
        } else if (returnType == int.class || returnType == Integer.class) {
            return 0;
        } else if (returnType == long.class || returnType == Long.class) {
            return 0L;
        } else if (returnType == float.class || returnType == Float.class) {
            return 0F;
        } else if (returnType == double.class || returnType == Double.class) {
            return 0D;
        } else if (returnType.isArray()) {
            return Array.newInstance(returnType.getComponentType(), 0);
        } else if (returnType.isAssignableFrom(ArrayList.class)) {
            return new ArrayList<Object>(0);
        } else if (returnType.isAssignableFrom(HashSet.class)) {
            return new HashSet<Object>(0);
        } else if (returnType.isAssignableFrom(HashMap.class)) {
            return new HashMap<Object, Object>(0);
        } else if (String.class.equals(returnType)) {
            return "";
        } else if (! returnType.isInterface()) {
            try {
                Object value = emptyInstances.get(returnType);
                if (value == null) {
                    value = returnType.newInstance();
                    emptyInstances.put(returnType, value);
                }
                Class<?> cls = value.getClass();
                while (cls != null && cls != Object.class) {
                    Field[] fields = cls.getDeclaredFields();
                    for (Field field : fields) {
                        Object property = getEmptyObject(field.getType(), emptyInstances, level + 1);
                        if (property != null) {
                            try {
                                if (! field.isAccessible()) {
                                    field.setAccessible(true);
                                }
                                field.set(value, property);
                            } catch (Throwable e) {
                            }
                        }
                    }
                    cls = cls.getSuperclass();
                }
                return value;
            } catch (Throwable e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static boolean isBeanPropertyReadMethod(Method method) {
        return method != null
                && Modifier.isPublic(method.getModifiers())
                && ! Modifier.isStatic(method.getModifiers())
                && method.getReturnType() != void.class
                && method.getDeclaringClass() != Object.class
                && method.getParameterTypes().length == 0
                && ((method.getName().startsWith("get") && method.getName().length() > 3)
                || (method.getName().startsWith("is") && method.getName().length() > 2));
    }

    public static String getPropertyNameFromBeanReadMethod(Method method) {
        if (isBeanPropertyReadMethod(method)) {
            if (method.getName().startsWith("get")) {
                return method.getName().substring(3, 4).toLowerCase()
                        + method.getName().substring(4);
            }
            if (method.getName().startsWith("is")) {
                return method.getName().substring(2, 3).toLowerCase()
                        + method.getName().substring(3);
            }
        }
        return null;
    }

    public static boolean isBeanPropertyWriteMethod(Method method) {
        return method != null
                && Modifier.isPublic(method.getModifiers())
                && ! Modifier.isStatic(method.getModifiers())
                && method.getDeclaringClass() != Object.class
                && method.getParameterTypes().length == 1
                && method.getName().startsWith("set")
                && method.getName().length() > 3;
    }

    public static String getPropertyNameFromBeanWriteMethod(Method method) {
        if (isBeanPropertyWriteMethod(method)) {
            return method.getName().substring(3, 4).toLowerCase()
                    + method.getName().substring(4);
        }
        return null;
    }

    public static boolean isPublicInstanceField(Field field) {
        return Modifier.isPublic(field.getModifiers())
                && !Modifier.isStatic(field.getModifiers())
                && !Modifier.isFinal(field.getModifiers())
                && !field.isSynthetic();
    }

    public static Map<String, Field> getBeanPropertyFields(Class cl) {
        Map<String, Field> properties = new HashMap();
        for (; cl != null; cl = cl.getSuperclass()) {
            Field[] fields = cl.getDeclaredFields();
            for (Field field : fields) {
                if ((!Modifier.isTransient(field.getModifiers())) && (!Modifier.isStatic(field.getModifiers()))) {
                    field.setAccessible(true);

                    properties.put(field.getName(), field);
                }
            }
        }
        return properties;
    }

    public static Map<String, Method> getBeanPropertyReadMethods(Class cl) {
        Map<String, Method> properties = new HashMap();
        for (; cl != null; cl = cl.getSuperclass()) {
            Method[] methods = cl.getDeclaredMethods();
            for (Method method : methods) {
                if (isBeanPropertyReadMethod(method)) {
                    method.setAccessible(true);
                    String property = getPropertyNameFromBeanReadMethod(method);
                    properties.put(property, method);
                }
            }
        }
        return properties;
    }

    private ReflectUtils(){}
}