package com.example.truesignscanner.model;

import androidx.annotation.NonNull;

public class Pack {
    public String name;
    public String numbersRecords = "0";
    public String memorySize = "0Kb";
    public boolean isSelected = false;
    public boolean isVisibleRadioButton = false;
    public boolean isWritingPackage = false;

    public Pack(String name, boolean isWritingPackage){
        this.name = name;
        this.isWritingPackage = isWritingPackage;
    }

    public Pack(String name, boolean isWritingPackage,String records, String memory){
        this.name = name;
        this.isWritingPackage = isWritingPackage;
        this.numbersRecords = records;
        this.memorySize = memory;
    }

    public Pack(Pack p) {
        this.name = p.name;
        this.isWritingPackage = p.isWritingPackage;
        this.numbersRecords = p.numbersRecords;
        this.isSelected = p.isSelected;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pack)) return false;
        final Pack that = (Pack) obj;
        return this.name.equals(that.name)
                && this.isWritingPackage == that.isWritingPackage
                && this.numbersRecords.equals(that.numbersRecords)
                && this.memorySize.equals(that.memorySize);
    }

    @NonNull
    @Override
    public String toString() {
        return "{" + "name: " + name + "; "
                + "counterRecords: " + numbersRecords + "; "
                + "isSelected: " + isSelected + ";"
                + "isWritingPackage: " + isWritingPackage + "}";
    }
}
