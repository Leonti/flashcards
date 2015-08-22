package rocks.leonti.flashcards.dao;

import java.io.Closeable;
import java.util.List;

import rocks.leonti.flashcards.model.Word;
import rocks.leonti.flashcards.model.WordSet;

public interface WordDao extends AutoCloseable {

    long createWordSet(WordSet wordSet);

    void createWords(List<Word> words);

    Word getWord(long id);

    List<WordSet> getSets();

    WordSet getSet(long setId);

    List<Word> getWords(long setId, int limit, int offset);

    List<Word> getWords(long setId, int limit, int offset, Word.Review review);

    List<Word> getWords(long[] wordIds);

    void increaseView(long wordId);

    void setReview(long wordId, Word.Review review);

    void open();

    void close();

    int getLearnedCount(long setId, int minViews);

    int getWordsToReviewCount(long setId);

    int getDoneWordsCount(long setId);
}
