package rocks.leonti.flashcards.model;


import java.util.List;

public class Word {

    public enum Type {
        NOUN("noun"), ADJECTIVE("adj"), VERB("verb"), ADVERB("adv");

        public final String shortened;

        Type(String shortened) {
            this.shortened = shortened;
        }
    }

    public enum Review { NONE, REVIEW, DONE }

    public final Long id;
    public final Long wordSetId;
    public final String word;
    public final List<Type> types;
    public final List<String> pronunciation;
    public final List<String> definition;
    public final List<String> usage;
    public final List<String> relatedWords;
    public final String info;
    public final int views;
    public final Review review;

    public Word(
            Long id,
            Long wordSetId,
            String word,
            List<Type> types,
            List<String> pronunciation,
            List<String> definition,
            List<String> usage,
            List<String> relatedWords,
            String info,
            int views,
            Review review) {
        this.id = id;
        this.wordSetId = wordSetId;
        this.word = word;
        this.types = types;
        this.pronunciation = pronunciation;
        this.definition = definition;
        this.usage = usage;
        this.relatedWords = relatedWords;
        this.info = info;
        this.views = views;
        this.review = review;
    }
}
