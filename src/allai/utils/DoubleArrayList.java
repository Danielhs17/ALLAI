/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package allai.utils;

import java.util.ArrayList;

/**
 * @author Daniel Alejandro Hurtado Simoes
 * Universidad de Málaga
 * TFG - Grado en Ingeniería Telemática
 */
public class DoubleArrayList<T,T2> {
    ArrayList<T> array1;   
    ArrayList<T2> array2;
    
    public DoubleArrayList(){
        array1 = new ArrayList<>();
        array2 = new ArrayList<>();
    }
    
    public void add(T elem1, T2 elem2){
        array1.add(elem1);
        array2.add(elem2);
    }
    
    public boolean containsInX(T elem){
        return array1.contains(elem);
    }
    
    public boolean containsInY(T2 elem){
        return array2.contains(elem);
    }
    
    public boolean containsPair(T elem, T2 elem2){
        return containsInX(elem) && containsInY(elem2);
    }
    
    public T getX(int index){
        return array1.get(index);
    }
    
    public T2 getY(int index){
        return array2.get(index);
    }
    
    public void remove(T elem1, T2 elem2){
        int index = -1;
        boolean found = false;
        for (T elems1 : array1){
            for (T2 elems2 : array2){
                if (elems1.equals(elem1) && elems2.equals(elem2) && !found){
                    index = array1.indexOf(elems1);
                    found = true;
                }
            }
        }
        if (found){
            remove(index);
        }
    }
    
    public void remove(int index){
        array1.remove(index);
        array2.remove(index);
    }
    
    public int size(){
        return array1.size();
    }

    public int indexOfX(T get) {
        return array1.indexOf(get);
    }
    
    public int indexOfY(T2 get) {
        return array2.indexOf(get);
    }

    public void clear(){
        array1.clear();
        array2.clear();
    }
  
}
