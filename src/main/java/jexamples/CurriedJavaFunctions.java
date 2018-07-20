package jexamples;

import java.util.function.Function;

public class CurriedJavaFunctions {

    private static void println(String msg) {
        System.out.println(msg);
    }

    public static void main(String[] args) {
        println("\n-----");
        new CurriedJavaFunctions();
        println("-----\n");
    }

    CurriedJavaFunctions() {
        println("----- java8Syntax");
        java8Syntax();
        println("----- java7Syntax");
        java7Syntax();
    }

    private void java8Syntax() {

        Function<Integer, Function<Integer, Function<Integer, Integer>>> sum3IntsCurried =
                a -> b -> c -> a + b + c;

        Integer result = sum3IntsCurried.apply(1).apply(2).apply(3);

        println("result = " + result);
    }

    private void java7Syntax() {

        Function<Integer, Function<Integer, Function<Integer, Integer>>> sum3IntsCurried =

                new Function<Integer, Function<Integer, Function<Integer, Integer>>>() {
                    @Override
                    public Function<Integer, Function<Integer, Integer>> apply(Integer a) {

                        return new Function<Integer, Function<Integer, Integer>>() {
                            @Override
                            public Function<Integer, Integer> apply(Integer b) {

                                return new Function<Integer, Integer>() {
                                    @Override
                                    public Integer apply(Integer c) {
                                        return a + b + c;
                                    }
                                };
                            }
                        };
                    }
                };

        Integer result = sum3IntsCurried.apply(1).apply(2).apply(3);

        println("result = " + result);
    }
}
