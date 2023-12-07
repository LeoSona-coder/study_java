package org.reol.study.collection.list;

import java.util.*;
import java.util.LinkedList;

public class ArrayListSample {


    public static void main(String[] args) {
        LinkedList<Integer> list = new LinkedList<>();
        list.add(1);
        list.get(1);


    }


    public static void addByNoArgConstructList() {
        // 默认构造函数
        ArrayList<String> list = new ArrayList<>();

        list.add("张三");
        list.add("李四");
        System.out.println(list);

    }

    /**
     * 检验一下添加集合时，到底扩容多少
     */
    public static void addAllTest() {


    }


}
