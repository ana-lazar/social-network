package domain;

import java.util.Objects;

public class Tuple<E1, E2> {
    private final E1 left;
    private final E2 right;

    public Tuple(E1 left, E2 right) {
        this.left = left;
        this.right = right;
    }

    public E1 getLeft() {
        return left;
    }

    public E2 getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "" + left + ',' + right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple)) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return (getLeft().equals(tuple.getLeft()) && getRight().equals(tuple.getRight())) ||
                (getLeft().equals(tuple.getRight()) && getRight().equals(tuple.getLeft()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLeft(), getRight());
    }
}
