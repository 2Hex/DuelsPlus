package me.hex.duelsplus;

public record Quadruplet<T, U, V, Z>(T first, U second, V third, Z fourth) {

    public T getFirst() {
        return first;
    }

    public U getSecond() {
        return second;
    }

    public V getThird() {
        return third;
    }

    public Z getFourth() {
        return fourth;
    }
}
