package rocks.leonti.flashcards.model;

public class WordSet {

    public final Long id;
    public final String globalId;
    public final String name;
    public final String description;
    public final int count;

    public WordSet(Long id, String globalId, String name, String description, int count) {
        this.id = id;
        this.globalId = globalId;
        this.name = name;
        this.description = description;
        this.count = count;
    }
}
