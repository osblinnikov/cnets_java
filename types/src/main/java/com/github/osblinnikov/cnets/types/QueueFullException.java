package com.github.osblinnikov.cnets.types;

public class QueueFullException extends Exception {
    String s = null;
    public QueueFullException(String s) {
        this.s = s;
    }

    @Override
    public String toString() {
        if(s != null){
            return s;
        }else{
            return "";
        }
    }
}
