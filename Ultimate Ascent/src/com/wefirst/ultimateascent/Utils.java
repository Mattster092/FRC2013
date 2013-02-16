package com.wefirst.ultimateascent;

public class Utils {
    public static double limit(double LIMIT, double val){
        if (val >= 0) {
            return Math.min(LIMIT, val);
        } else {
            return Math.max(-LIMIT, val);
        }
    }
}
