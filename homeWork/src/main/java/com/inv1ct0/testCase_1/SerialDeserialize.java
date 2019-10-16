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
    private byte[] bytes;
    private Set<Object> cache;

    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            return bytes;
        }
        checkCycleLink(obj);
        writeObjects(obj);
        return bytes;
    }

    private void checkCycleLink(Object object) {
        cache = new HashSet<>();
        try {
            fieldTraversal(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InfinityException e) {
            e.printStackTrace();
            System.exit(e.errorCode);
        }
    }

    private void fieldTraversal(Object object) throws IllegalAccessException, InfinityException {
        if (cache.add(object)) {
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (!(field.getType().isPrimitive() || Number.class.isAssignableFrom(field.getType()) ||
                        field.getType().equals(String.class) || field.getType().equals(Character.class) ||
                        field.getType().equals(Boolean.class) || field.getType().equals(Instant.class))) {
                    if (List.class.isAssignableFrom(field.getType())) {
                        if (field.get(object) != null) {
                            for (Object item : (List) field.get(object)) {
                                if (item != null) {
                                    fieldTraversal(item);
                                }
                            }
                        }
                    } else if (Set.class.isAssignableFrom(field.getType())) {
                        if (field.get(object) != null) {
                            for (Object item : (Set) field.get(object)) {
                                if (item != null) {
                                    fieldTraversal(item);
                                }
                            }
                        }
                    } else if (Map.class.isAssignableFrom(field.getType())) {
                        if (field.get(object) != null) {
                            for (Object entry : ((Map) field.get(object)).entrySet()) {
                                if (((Map.Entry) entry).getValue() != null) {
                                    fieldTraversal(((Map.Entry) entry).getValue());
                                }
                            }
                        }
                    } else {
                        if (field.get(object) != null) {
                            fieldTraversal(field.get(object));
                        }
                    }
                    cache.remove(object);
                }
            }
        } else {
            throw new InfinityException(1, "A cycle link is detected!");
        }
    }

    private void writeObjects(Object obj) {
        cache = new HashSet<>();
        try (ByteArrayOutputStream buff = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(buff)) {
            write(obj, out);
            bytes = buff.toByteArray();
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void write(Object object, DataOutputStream out) throws IllegalAccessException, IOException {
        Class clazz = object.getClass();
        out.writeUTF(clazz.getName());
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (!Modifier.isStatic(field.getModifiers())) {
                String className = field.getType().getName();
                Object objFields = field.get(object);
                if (objFields == null) {
                    out.writeUTF("null");
                } else {
                    if (!field.getType().isPrimitive()) {
                        out.writeUTF("notnull");
                    }
                    if (("byte".equals(className)) || ("java.lang.Byte".equals(className))) {
                        out.writeByte((byte) objFields);
                    } else if (("short".equals(className)) || ("java.lang.Short".equals(className))) {
                        out.writeShort((short) objFields);
                    } else if (("char".equals(className)) || ("java.lang.Character".equals(className))) {
                        out.writeChar((char) objFields);
                    } else if (("int".equals(className)) || ("java.lang.Integer".equals(className))) {
                        out.writeInt((int) objFields);
                    } else if (("long".equals(className)) || ("java.lang.Long".equals(className))) {
                        out.writeLong((long) objFields);
                    } else if (("float".equals(className)) || ("java.lang.Float".equals(className))) {
                        out.writeFloat((float) objFields);
                    } else if (("double".equals(className)) || ("java.lang.Double".equals(className))) {
                        out.writeDouble((double) objFields);
                    } else if (("boolean".equals(className)) || ("java.lang.Boolean".equals(className))) {
                        out.writeBoolean((boolean) objFields);
                    } else if ("java.lang.String".equals(className)) {
                        out.writeUTF(objFields.toString());
                    } else if ("java.math.BigDecimal".equals(className)) {
                        out.writeUTF(objFields.toString());
                    } else if ("java.time.Instant".equals(className)) {
                        out.writeUTF(objFields.toString());
                    } else if (List.class.isAssignableFrom(field.getType())) {
                        out.writeUTF(objFields.getClass().getName());
                        out.writeInt(((List) objFields).size());
                        for (Object item : (List) objFields) {
                            if (item == null) {
                                out.writeUTF("null");
                            } else {
                                out.writeUTF("notnull");
                                write(item, out);
                            }
                        }
                    } else if (Set.class.isAssignableFrom(field.getType())) {
                        out.writeUTF(objFields.getClass().getName());
                        out.writeInt(((Set) objFields).size());
                        for (Object item : (Set) objFields) {
                            if (item == null) {
                                out.writeUTF("null");
                            } else {
                                out.writeUTF("notnull");
                                write(item, out);
                            }
                        }
                    } else if (Map.class.isAssignableFrom(field.getType())) {
                        out.writeUTF(objFields.getClass().getName());
                        out.writeInt(((Map) objFields).size());
                        for (Object entry : ((Map) objFields).entrySet()) {
                            if (((Map.Entry) entry).getKey() == null) {
                                out.writeUTF("null");
                            } else {
                                out.writeUTF("notnull");
                                write(((Map.Entry) entry).getKey(), out);
                            }
                            if (((Map.Entry) entry).getValue() == null) {
                                out.writeUTF("null");
                            } else {
                                out.writeUTF("notnull");
                                write(((Map.Entry) entry).getValue(), out);
                            }
                        }
                    } else {
                        write(objFields, out);
                    }
                }
            }
        }
    }

    @Override
    public Object deserialize(byte[] data) {
        Object obj = null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             DataInputStream in = new DataInputStream(bais)) {
            try {
                obj = readObjects(in);
            } catch (NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return obj;
    }

    private Object readObjects(DataInputStream in) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Object newObject = Class.forName(in.readUTF()).getDeclaredConstructor().newInstance();
        Field[] fields = newObject.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if ("byte".equals(field.getType().getName())) {
                field.setByte(newObject, in.readByte());
            } else if ("short".equals(field.getType().getName())) {
                field.setShort(newObject, in.readShort());
            } else if ("char".equals(field.getType().getName())) {
                field.setChar(newObject, in.readChar());
            } else if ("int".equals(field.getType().getName())) {
                field.setInt(newObject, in.readInt());
            } else if ("long".equals(field.getType().getName())) {
                field.setLong(newObject, in.readLong());
            } else if ("float".equals(field.getType().getName())) {
                field.setFloat(newObject, in.readFloat());
            } else if ("double".equals(field.getType().getName())) {
                field.setDouble(newObject, in.readDouble());
            } else if ("boolean".equals(field.getType().getName())) {
                field.setBoolean(newObject, in.readBoolean());
            } else if ("java.lang.Byte".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(in.readUTF()) ? null : in.readByte()));
            } else if ("java.lang.Short".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(in.readUTF()) ? null : in.readShort()));
            } else if ("java.lang.Character".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(in.readUTF()) ? null : in.readChar()));
            } else if ("java.lang.Integer".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(in.readUTF()) ? null : in.readInt()));
            } else if ("java.lang.Long".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(in.readUTF()) ? null : in.readLong()));
            } else if ("java.lang.Float".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(in.readUTF()) ? null : in.readFloat()));
            } else if ("java.lang.Double".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(in.readUTF()) ? null : in.readDouble()));
            } else if ("java.lang.Boolean".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(in.readUTF()) ? null : in.readBoolean()));
            } else if ("java.lang.String".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(in.readUTF()) ? null : in.readUTF()));
            } else if ("java.math.BigDecimal".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(in.readUTF()) ? null : new BigDecimal(in.readUTF())));
            } else if ("java.time.Instant".equals(field.getType().getName())) {
                field.set(newObject, ("null".equals(in.readUTF()) ? null : Instant.parse(in.readUTF())));
            } else if (List.class.isAssignableFrom(field.getType())) {
                Object objField = "null".equals(in.readUTF()) ? null : Class.forName(in.readUTF()).getDeclaredConstructor().newInstance();
                if (objField == null) {
                    field.set(newObject, null);
                } else {
                    int length = in.readInt();
                    for (int i = 0; i < length; i++) {
                        ((List) objField).add("null".equals(in.readUTF()) ? null : readObjects(in));
                    }
                    field.set(newObject, objField);
                }
            } else if (Set.class.isAssignableFrom(field.getType())) {
                Object objField = "null".equals(in.readUTF()) ? null : Class.forName(in.readUTF()).getDeclaredConstructor().newInstance();
                if (objField == null) {
                    field.set(newObject, null);
                } else {
                    int length = in.readInt();
                    for (int i = 0; i < length; i++) {
                        ((Set) objField).add("null".equals(in.readUTF()) ? null : readObjects(in));
                    }
                    field.set(newObject, objField);
                }
            } else if (Map.class.isAssignableFrom(field.getType())) {
                Object objField = "null".equals(in.readUTF()) ? null : Class.forName(in.readUTF()).getDeclaredConstructor().newInstance();
                if (objField == null) {
                    field.set(newObject, null);
                } else {
                    int length = in.readInt();
                    Object key;
                    String strType;
                    for (int i = 0; i < length; i++) {
                        if ("null".equals(in.readUTF())) {
                            key = null;
                        } else {
                            strType = in.readUTF();
                            switch (strType) {
                                case "java.lang.Byte":
                                    key = in.readByte();
                                    break;
                                case "java.lang.Short":
                                    key = in.readShort();
                                    break;
                                case "java.lang.Character":
                                    key = in.readChar();
                                    break;
                                case "java.lang.Integer":
                                    key = in.readInt();
                                    break;
                                case "java.lang.Long":
                                    key = in.readLong();
                                    break;
                                case "java.lang.Float":
                                    key = in.readFloat();
                                    break;
                                case "java.lang.Double":
                                    key = in.readDouble();
                                    break;
                                case "java.lang.Boolean":
                                    key = in.readBoolean();
                                    break;
                                case "java.lang.String":
                                    key = in.readUTF();
                                    break;
                                case "java.math.BigDecimal":
                                    key = new BigDecimal(in.readUTF());
                                    break;
                                case "java.time.Instant":
                                    key = Instant.parse(in.readUTF());
                                    break;
                                default:
                                    key = readObjects(in);
                                    break;
                            }
                        }
                        ((Map) objField).put(key, "null".equals(in.readUTF()) ? null : readObjects(in));
                    }
                    field.set(newObject, objField);
                }
            } else {
                Object objField = "null".equals(in.readUTF()) ? null : readObjects(in);
                field.set(newObject, objField);
            }
        }
        return newObject;
    }

    private static class InfinityException extends Exception {
        private int errorCode;
        private String message;

        InfinityException(int errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }
}