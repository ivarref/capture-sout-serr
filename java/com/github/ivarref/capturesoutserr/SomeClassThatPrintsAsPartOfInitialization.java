package com.github.ivarref.capturesoutserr;

public class SomeClassThatPrintsAsPartOfInitialization {

    public static final int x = getInt();

    public static final AnotherClassThatPrintsAsPartOfInitialization foo = new AnotherClassThatPrintsAsPartOfInitialization();

    public final AnotherClassThatPrintsAsPartOfInitialization bar = new AnotherClassThatPrintsAsPartOfInitialization();

    public static int getInt() {
        new AnotherClassThatPrintsAsPartOfInitialization();
        System.out.println("static SomeClassThatPrintsAsPartOfInitialization.getInt()");
        return 1;
    }

    static {
        {
            new AnotherClassThatPrintsAsPartOfInitialization();
            System.out.println("static INIT SomeClassThatPrintsAsPartOfInitialization");
        }
    }
}
