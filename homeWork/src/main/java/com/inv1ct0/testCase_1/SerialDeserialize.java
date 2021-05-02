/*
Необходимо написать класс который сериализует/десериализует Java Beans.

В качестве полей в Bean могут быть:
     простые типы, 
     String, Instant, BigDecimal и все классы обертки для простых типов (Short, Integer, Long и т.д.)
     Beans, 
     коллекции Beans: List, Map, Set

В случае наличия циклических ссылок выкинуть exception. Класс должен имплементировать следующий интерфейс:

interface SuperEncoder {
        byte[] serialize(Object anyBean);
        Object deserialize(byte[] data);  

При выполнении задания нельзя использовать готовые сериализаторы, такие как JSON, Protobuf, Java serialization API.
 */
package com.inv1ct0.testCase_1;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SerialDeserialize implements SuperEncoder{
    private byte[] bytes;         // Массив байтов, который возвращает метод serialize
    private Set<Object> cache;

    @Override
    public byte[] serialize(Object anyBeans) {
        if (anyBeans == null) {
            return bytes;
        }
        try {
            findCycleLink(anyBeans);
        } catch (IllegalAccessException | CycleLinkException e) {
            e.printStackTrace();
        }
        writeObject(anyBeans);
        return bytes;         // Из метода возвращается массив байтов byte[]
    }

    private void findCycleLink(Object anyBeans) throws IllegalAccessException, CycleLinkException {
        cache = new HashSet<>();
        if(cache.add(anyBeans)) {
            Field[] fields = anyBeans.getClass().getDeclaredFields();
            for(Field field : fields) {
                field.setAccessible(true);
                if((field.getType().isPrimitive() || Number.class.isAssignableFrom(field.getType()) || field.getType().equals(String.class)
                || field.getType().equals(Character.class) || field.getType().equals(Boolean.class) || field.getType().equals(Instant.class))) {
                    if(List.class.isAssignableFrom(field.getType())) {
                        if(field.get(anyBeans) != null) {
                            for(Object object : (List) field.get(anyBeans)) {
                                if(object != null) {
                                    findCycleLink(object);
                                }
                            }
                        }
                    } if(Set.class.isAssignableFrom(field.getType())) {
                        if(field.get(anyBeans) != null) {
                            for(Object object : (Set) field.get(anyBeans)) {
                                if(object != null) {
                                    findCycleLink(object);
                                }
                            }
                        }
                    } if(Map.class.isAssignableFrom(field.getType())) {
                        if(field.get(anyBeans) != null) {
                            for(Object object : ((Map) field.get(anyBeans)).entrySet()) {
                                if(((Map.Entry) object).getValue() != null) {
                                    findCycleLink(((Map.Entry) object).getValue());
                                }
                            }
                        }
                    } else {
                        if(field.get(anyBeans) != null) {
                            findCycleLink(field.get(anyBeans));
                        }
                    }
                    cache.remove(anyBeans);
                }
            }
        }
        else {
            throw new CycleLinkException();
        }
    }

    private void writeObject(Object anyBeans) {
        cache = new HashSet<>();
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(buffer)) {
            write(anyBeans, dataOutputStream);
            bytes = buffer.toByteArray();
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void write(Object anyBeans, DataOutputStream dataOutputStream) throws IOException, IllegalAccessException {
        Class clazz = anyBeans.getClass(); //получение объекта типа Class
        dataOutputStream.writeUTF(clazz.getName());
        Field[] fields = clazz.getDeclaredFields(); //получить все поля класса. getDeclaredFields(), а не getFields() для private и protected полей
        for (Field field : fields) {
            field.setAccessible(true); //дать каждому полю доступ для работы с ним
            if(!Modifier.isStatic(field.getModifiers())) { //получение модификаторов класса и действия в слуае если он не Static
                String className = field.getType().getName();
                Object objectFields = field.get(anyBeans);
                if(objectFields == null) {
                    dataOutputStream.writeUTF("null");
                } else {
                    if(!field.getType().isPrimitive()) {
                        dataOutputStream.writeUTF("not Null");
                    }
                    if(("byte".equals(className)) || ("java.lang.Byte".equals(className))) {
                        dataOutputStream.writeByte((byte) objectFields);
                    } else if(("short".equals(className)||("java.lang.Short".equals(className)))) {
                        dataOutputStream.writeShort((short) objectFields);
                    } else if(("char".equals(className)) || ("java.lang.Character".equals(className))) {
                        dataOutputStream.writeChar((char) objectFields);
                    } else if(("int".equals(className)) || ("java.lang.Integer".equals(className))) {
                        dataOutputStream.writeInt((int) objectFields);
                    } else if(("long".equals(className)) || ("java.lang.Long".equals(className))) {
                        dataOutputStream.writeLong((long) objectFields);
                    } else if(("float".equals(className)) || ("java.lang.Float".equals(className))) {
                        dataOutputStream.writeFloat((float) objectFields);
                    } else if(("double".equals(className)) || ("java.lang.Double".equals(className))) {
                        dataOutputStream.writeDouble((double) objectFields);
                    } else if(("boolean".equals(className)) || ("java.lang.Boolean".equals(className))) {
                        dataOutputStream.writeBoolean((boolean) objectFields);
                    } else if("java.lang.String".equals(className)) {
                        dataOutputStream.writeUTF(objectFields.toString());
                    } else if("java.math.BigDecimal".equals(className)) {
                        dataOutputStream.writeUTF(objectFields.toString());
                    } else if("java.time.Instant".equals(className)) {
                        dataOutputStream.writeUTF(objectFields.toString());
                        /*
                        определяем принадлежность к классу.
                        isAssignableFrom вместо instanceOf, потому что для instanceof тип класса,
                        на принадлежность которому мы проверяем, должен быть известен на этапе компиляции.
                         */
                    } else if(List.class.isAssignableFrom(field.getType())) {
                        dataOutputStream.writeUTF(objectFields.getClass().getName());
                        dataOutputStream.writeInt(((List)objectFields).size());
                        for(Object obj : (List)objectFields) {
                            if(obj == null) {
                                dataOutputStream.writeUTF("null");
                            } else {
                                dataOutputStream.writeUTF("not Null");
                                write(obj, dataOutputStream);
                            }
                        }
                    } else if(Set.class.isAssignableFrom(field.getType())) {
                        dataOutputStream.writeUTF(objectFields.getClass().getName());
                        dataOutputStream.writeInt(((Set)objectFields).size());
                        for(Object obj : (Set) objectFields) {
                            if(obj == null) {
                                dataOutputStream.writeUTF("null");
                            } else {
                                dataOutputStream.writeUTF("not Null");
                                write(obj, dataOutputStream);
                            }
                        }
                    } else if(Map.class.isAssignableFrom(field.getType())) {
                        dataOutputStream.writeUTF(objectFields.getClass().getName());
                        dataOutputStream.writeInt(((Map) objectFields).size());
                        for(Object obj : ((Map) objectFields).entrySet()) {
                            /*
                            entrySet возвращает набор пары ключ-значение
                            проходим по всем значения Map с помощью getKey() и getValue()
                             */
                            if(((Map.Entry)obj).getKey() == null) {
                                dataOutputStream.writeUTF("null");
                            } else {
                                dataOutputStream.writeUTF("not Null");
                                write(((Map.Entry) obj).getKey(), dataOutputStream);
                            }
                            if(((Map.Entry) obj).getValue() == null) {
                                dataOutputStream.writeUTF("null");
                            } else {
                                dataOutputStream.writeUTF("not Null");
                                write(((Map.Entry)obj).getValue(), dataOutputStream);
                            }
                        }
                    } else {
                        write(objectFields, dataOutputStream);
                    }
                }
            }
        }
    }

    @Override
    public Object deserialize(byte[] data) {
        Object object = null;
        // Создание объекта класса ByteArrayInputStream в конструктор которого передается
        // полученный в методе serialize массив байтов, для восстановления объекта
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
             // и объекта класса DataInputStream,
             // в конструктор которого передается ссылка на поток ByteArrayInputStream
                DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream)) {
                    object =readObject(dataInputStream);             // Чтение данных из потока, и запись их в объект
                } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return object;         // Из метода возвращается объект Object
    }

    private Object readObject(DataInputStream dataInputStream) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object newObject = Class.forName(dataInputStream.readUTF()).getDeclaredConstructor().newInstance();
        /*
        получив класс getDeclaredConstructor().newInstance() вернет Object который будет создан по описанию полученного класса
        getDeclaredConstructor().newInstance() вместо устаревшего newInstance, чтобы учесть возможные исключения при нулевом конструкторе
        рассматриваемого класса
         */
        Field[] fields = newObject.getClass().getDeclaredFields(); //получить все поля класса. getDeclaredFields(), а не getFields() для private и protected полей
        for (Field field : fields) {
            field.setAccessible(true);  //дать каждому полю доступ для работы с ним
            // десериализация примитивных типов и их оберток
            if("byte".equals(field.getType().getName())) {
                field.setByte(newObject, dataInputStream.readByte());
            } else if("short".equals(field.getType().getName())) {
                field.setShort(newObject, dataInputStream.readShort());
            } else if("char".equals(field.getType().getName())) {
                field.setChar(newObject, dataInputStream.readChar());
            } else if("int".equals(field.getType().getName())) {
                field.setInt(newObject, dataInputStream.readInt());
            } else if("long".equals(field.getType().getName())) {
                field.setLong(newObject, dataInputStream.readLong());
            } else if("float".equals(field.getType().getName())) {
                field.setFloat(newObject, dataInputStream.readFloat());
            } else if("double".equals(field.getType().getName())) {
                field.setDouble(newObject, dataInputStream.readDouble());
            } else if("boolean".equals(field.getType().getName())) {
                field.setBoolean(newObject, dataInputStream.readBoolean());
            } else if("java.lang.Byte".equals(field.getType().getName())) {
                field.set(newObject,("null".equals(dataInputStream.readUTF()) ? null : dataInputStream.readByte()));
            } else if("java.lang.Short".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(dataInputStream.readUTF()) ? null : dataInputStream.readShort()));
            } else if("java.lang.Character".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(dataInputStream.readUTF())? null: dataInputStream.readChar()));
            } else if("java.lang.Integer".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(dataInputStream.readUTF()) ?null : dataInputStream.readInt()));
            } else if("java.lang.Float".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(dataInputStream.readUTF()) ? null : dataInputStream.readFloat()));
            } else if("java.lang.Double".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(dataInputStream.readUTF()) ? null : dataInputStream.readDouble()));
            } else if("java.lang.Boolean".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(dataInputStream.readUTF()) ? null : dataInputStream.readBoolean()));
                // десериализация String
            } else if("java.lang.String".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(dataInputStream.readUTF()) ? null : dataInputStream.readUTF()));
                // десериализация BigDecimal
            } else if("java.math.BigDecimal".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(dataInputStream.readUTF()) ? null : new BigDecimal(dataInputStream.readUTF())));
                // десериализация Instant
            } else if("java.time.Instant".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(dataInputStream.readUTF()) ? null : Instant.parse(dataInputStream.readUTF())));
                /*
                определяем принадлежность к классу.
                isAssignableFrom вместо instanceOf, потому что для instanceof тип класса,
                на принадлежность которому мы проверяем, должен быть известен на этапе компиляции.
                */
            } else if(List.class.isAssignableFrom(field.getType())) {
                /*
                получив класс getDeclaredConstructor().newInstance() вернет Object который будет создан по описанию полученного класса
                getDeclaredConstructor().newInstance() вместо устаревшего newInstance, чтобы учесть возможные исключения при нулевом конструкторе
                рассматриваемого класса
                */
                Object objectField = "null".equals(dataInputStream.readUTF()) ? null : Class.forName(dataInputStream.readUTF()).getDeclaredConstructor().newInstance();
                if(objectField == null) {
                    field.set(newObject, null);
                } else {
                    int lengthList = dataInputStream.readInt();
                    for (int i = 0; i < lengthList; i++) {
                        ((List) objectField).add("null".equals(dataInputStream.readUTF()) ? null : readObject(dataInputStream));
                    }
                    field.set(newObject, objectField);
                }
            } else if(Set.class.isAssignableFrom(field.getType())) {
                Object objectField = "null".equals(dataInputStream.readUTF()) ? null : Class.forName(dataInputStream.readUTF()).getDeclaredConstructor().newInstance();
                if(objectField == null) {
                    field.set(newObject, null);
                } else {
                    int lengthSet = dataInputStream.readInt();
                    for (int i = 0; i < lengthSet; i++) {
                        ((Set) objectField).add("null".equals(dataInputStream.readUTF()) ? null : readObject(dataInputStream));
                    }
                    field.set(newObject, objectField);
                }
            } else if(Map.class.isAssignableFrom(field.getType())) {
                Object objectField = "null".equals(dataInputStream.readUTF()) ? null : Class.forName(dataInputStream.readUTF()).getDeclaredConstructor().newInstance();
                if(objectField == null) {
                    field.set(newObject, null);
                } else {
                    int lengthMap = dataInputStream.readInt();
                    Object key;
                    String stringType;
                    for (int i = 0; i < lengthMap; i++) {
                        if("null".equals(dataInputStream.readUTF())) {
                            key = null;
                        } else {
                            stringType = dataInputStream.readUTF();
                            switch (stringType) {
                                case "java.lang.Byte":
                                    key = dataInputStream.readByte();
                                    break;
                                case "java.lang.Short":
                                    key = dataInputStream.readShort();
                                    break;
                                case "java.lang.Character":
                                    key = dataInputStream.readChar();
                                    break;
                                case "java.lang.Integer":
                                    key = dataInputStream.readInt();
                                    break;
                                case "java.lang.Long":
                                    key = dataInputStream.readLong();
                                    break;
                                case "java.lang.Float":
                                    key = dataInputStream.readFloat();
                                    break;
                                case "java.lang.Double":
                                    key = dataInputStream.readDouble();
                                    break;
                                case "java.lang.Boolean":
                                    key = dataInputStream.readBoolean();
                                    break;
                                case "java.lang.String":
                                    key = dataInputStream.readUTF();
                                    break;
                                case "java.math.BigDecimal":
                                    key = new BigDecimal(dataInputStream.readUTF());
                                    break;
                                case "java.time.Instant":
                                    key = Instant.parse(dataInputStream.readUTF());
                                    break;
                                default:
                                    key = readObject(dataInputStream);
                                    break;
                            }
                        }
                        ((Map)objectField).put(key, "null".equals(dataInputStream.readUTF()) ? null : readObject(dataInputStream));
                    }
                    field.set(newObject, objectField);
                }
            }
            else {
                Object objectField = "null".equals(dataInputStream.readUTF()) ? null : readObject(dataInputStream);
                field.set(newObject, objectField);
            }
        }
        return newObject;
    }
}
