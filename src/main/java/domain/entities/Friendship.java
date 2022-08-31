package domain.entities;

import domain.Tuple;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Friendship extends Entity<Tuple<Long, Long>> {
    private final LocalDateTime date;

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return date.format(formatter) + "," + getId().getLeft().toString() + "," + getId().getRight().toString();
    }

    public Friendship(LocalDateTime date) {
        this.date = date;
    }

    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friendship)) return false;
        Friendship that = (Friendship) o;
        return (getId().getLeft().equals(((Friendship) o).getId().getLeft()) && getId().getRight().equals(((Friendship) o).getId().getRight())) || (
                getId().getLeft().equals(((Friendship) o).getId().getRight()) && getId().getRight().equals(((Friendship) o).getId().getLeft())
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
