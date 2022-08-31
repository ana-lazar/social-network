package utils;

public interface Observable<E extends MainEvent> {
    void addObserver(Observer<E> observer);

    void removeObserver(Observer<E> observer);

    void notifyObservers(E event);
}
