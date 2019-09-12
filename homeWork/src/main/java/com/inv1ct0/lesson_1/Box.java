package com.inv1ct0.lesson_1;

import java.util.ArrayList;
import java.util.Arrays;

public class Box<T extends Fruit> {
    //Для хранения фруктов внутри коробки можно использовать ArrayList;
    private ArrayList<T> fruits;

    //конструктор
    Box(T... fruits) {
        this.fruits = new ArrayList<T>(Arrays.asList(fruits));
    }
    //метод добавления фруктов
    public void add(T... fruits) {
        this.fruits.addAll(Arrays.asList(fruits));
    }
    //метод удаления из корзины фрукта
    public void remove(T... items) {
        for (T item: items) this.fruits.remove(item);
    }
    //удалить все из корзины
    private void clear() {
        fruits.clear();
    }
    //метод, который высчитывает вес коробки, зная количество фруктов и вес одного фрукта
    private float getWeight() {
        if(fruits.size() == 0) return 0; //корзина пуста. Ничего не делаем
        float weight = 0;
        for (T fruit: fruits) {
            weight = weight+fruit.getWeight();
        }
        return weight;
    }
    //метод, который позволяет сравнить текущую коробку с той, которую подадут в compare в качестве параметра
    boolean compare(Box box) {
        return this.getWeight() == box.getWeight();
    }
    //метод, который позволяет пересыпать фрукты из текущей коробки в другую
    void shift(Box<? super T> box) {
        box.fruits.addAll(this.fruits);
        clear(); //удаляем все из коробки, из которой пересыпали
    }
}