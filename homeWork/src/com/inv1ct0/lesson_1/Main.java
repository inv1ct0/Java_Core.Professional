package com.inv1ct0.lesson_1;

import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        Integer[] array = new Integer[3];

        array[0] = 25;
        array[1] = 72;
        array[2] = 58;

        System.out.println("Массив до взаимодействия: "+ Arrays.toString(array));
        swapElements(array, 0, 2); //смена элементов местами
        System.out.println("Поменяем 1й и 3й элементы массива местами: "+ Arrays.toString(array));

        ArrayList<Integer> arrayList = arrToArrayList(array); //преобразовываем массив в ArrayList

        /**
         * Задание 3
         * Есть классы Fruit -> Apple, Orange (больше фруктов не надо);
         * Класс Box, в который можно складывать фрукты. Коробки условно сортируются по типу фрукта,
         * поэтому в одну коробку нельзя сложить и яблоки, и апельсины;
         * Для хранения фруктов внутри коробки можно использовать ArrayList;
         * Сделать метод getWeight(), который высчитывает вес коробки,
         * зная количество фруктов и вес одного фрукта (вес яблока – 1.0f, апельсина – 1.5f. Не важно, в каких это единицах);
         * Внутри класса Коробка сделать метод compare, который позволяет сравнить текущую коробку с той,
         * которую подадут в compare в качестве параметра, true – если она равны по весу,
         * false – в противном случае (коробки с яблоками мы можем сравнивать с коробками с апельсинами);
         * Написать метод, который позволяет пересыпать фрукты из текущей коробки в другую
         * (помним про сортировку фруктов: нельзя яблоки высыпать в коробку с апельсинами).
         * Соответственно, в текущей коробке фруктов не остается, а в другую перекидываются объекты, которые были в этой коробке;
         * Не забываем про метод добавления фрукта в коробку.
         */

        Apple apple1 = new Apple();
        Apple apple2 = new Apple();
        Apple apple3 = new Apple();

        Orange orange1 = new Orange();
        Orange orange2 = new Orange();
        Orange orange3 = new Orange();
        Orange orange4 = new Orange();

        Box<Apple> box1 = new Box<Apple>(apple1,apple2);
        Box<Orange> box2 = new Box<Orange>(orange1,orange2,orange3,orange4);
        box1.add(apple3); // добавляем в ящик яблоко через метод
        box2.add(orange1); //удаляем из ящика апельсин orange1

        System.out.println(box1.compare(box2));
        if(box1.compare(box2)) {
            System.out.println("Коробки равны по весу");
        } else System.out.println("Коробки не равны по весу");

        Box<Apple> box3 = new Box<Apple>();
        box1.shift(box3);
    }

    /**
     * Задание 1
     * Написать метод, который меняет два элемента массива местами.
     * (массив может быть любого ссылочного типа);
     */

    private static void swapElements(Object[] _array, int num1, int num2) {
        Object t = _array[num1];
        _array[num1] = _array[num2];
        _array[num2] = t;
    }

    /**
     * Задание 2
     * Написать метод, который преобразует массив в ArrayList;
     */

    private static <T> ArrayList<T> arrToArrayList(T[] _array) {
        return new ArrayList<T>(Arrays.asList(_array));
    }
}