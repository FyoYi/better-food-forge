package com.fyoyi.betterfood.util;

public class ToolStats {

    private int LEVEL;
    private int USES;
    private int SPEED;
    private int ENCHANTMENTVALUE;
    private float ATTACKDAMAGE;

    public ToolStats(int LEVEL, int USES, int SPEED, float ATTACKDAMAGE, int ENCHANTMENTVALUE){
        this.LEVEL = LEVEL;
        this.USES = USES;
        this.SPEED = SPEED;
        this.ATTACKDAMAGE = ATTACKDAMAGE;
        this.ENCHANTMENTVALUE = ENCHANTMENTVALUE;
    }

    public int getLEVEL(){
        return LEVEL;
    }
    public int getUSES(){
        return USES;
    }
    public int getSPEED(){
        return SPEED;
    }
    public float getATTACKDAMAGE(){
        return ATTACKDAMAGE;
    }
    public int getENCHANTMENTVALUE(){
        return ENCHANTMENTVALUE;
    }

}
