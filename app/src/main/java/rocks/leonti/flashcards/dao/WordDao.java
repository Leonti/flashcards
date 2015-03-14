package rocks.leonti.flashcards.dao;

import java.io.Closeable;
import java.util.List;

import rocks.leonti.flashcards.model.Word;
import rocks.leonti.flashcards.model.WordSet;

public interface WordDao extends AutoCloseable {

    long createWordSet(WordSet wordSet);

    long createWord(Word word);

    Word getWord(long id);

    List<WordSet> getSets();

    List<Word> getWords(long setId, int limit, int offset);

    void increaseView(long wordId);

    void setReview(long wordId, Word.Review review);

    void open();

    void close();
}
