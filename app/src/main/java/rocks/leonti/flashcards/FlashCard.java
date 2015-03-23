package rocks.leonti.flashcards;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import rocks.leonti.flashcards.dao.WordDao;
import rocks.leonti.flashcards.dao.WordDaoImpl;
import rocks.leonti.flashcards.model.Word;


public class FlashCard extends Fragment {

    private static final String WORD_ID = "wordId";

    private Word word;

    public static FlashCard newInstance(long wordId) {
        FlashCard fragment = new FlashCard();
        Bundle args = new Bundle();
        args.putLong(WORD_ID, wordId);
        fragment.setArguments(args);
        return fragment;
    }

    public FlashCard() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            try (WordDao wordDao = new WordDaoImpl(this.getActivity())) {
                wordDao.open();

                word = wordDao.getWord(getArguments().getLong(WORD_ID));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_flash_card, container, false);

        ((TextView) view.findViewById(R.id.word_front)).setText(word.word);
        ((TextView) view.findViewById(R.id.types_front)).setText(listOfTypesToString(word.types));
        ((TextView) view.findViewById(R.id.pronunciation_front)).setText(toStringList(word.pronunciation));

        ((TextView) view.findViewById(R.id.word)).setText(word.word);
        ((TextView) view.findViewById(R.id.types)).setText(listOfTypesToString(word.types));
        ((TextView) view.findViewById(R.id.pronunciation)).setText(toStringList(word.pronunciation));
        ((TextView) view.findViewById(R.id.definition)).setText(toStringList(word.definition));
        ((TextView) view.findViewById(R.id.usage)).setText(toStringList(word.usage));

        if (word.relatedWords.size() > 0) {
            ((TextView) view.findViewById(R.id.related_words)).setText(toStringList(word.relatedWords));
        } else {
            view.findViewById(R.id.related_words_label).setVisibility(View.GONE);
            view.findViewById(R.id.related_words).setVisibility(View.GONE);
        }
        final GestureDetectorCompat gestureDetector = new GestureDetectorCompat(getActivity(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {

                FlipAnimation flipAnimation = new FlipAnimation(view.findViewById(R.id.container_front_card), view.findViewById(R.id.container_back_card));
                view.startAnimation(flipAnimation);

                return true;
            }
        });

        view.findViewById(R.id.container_front_card).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        final GestureDetectorCompat gestureDetectorBack = new GestureDetectorCompat(getActivity(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                FlipAnimation flipAnimation = new FlipAnimation(view.findViewById(R.id.container_front_card), view.findViewById(R.id.container_back_card));
                flipAnimation.reverse();
                view.startAnimation(flipAnimation);

                return true;
            }
        });

        view.findViewById(R.id.container_back_card).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetectorBack.onTouchEvent(event);
            }
        });

        return view;
    }

    private String toStringList(List<String> list) {
        String toReturn = "";
        for (int i = 0; i < list.size(); i++) {

            toReturn += list.get(i);
            if (i != list.size() - 1) {
                toReturn += "; ";
            }
        }

        return toReturn;
    }

    private String listOfTypesToString(List<Word.Type> list) {
        String toReturn = "(";

        for (int i = 0; i < list.size(); i++) {
            toReturn += list.get(i).shortened;
            if (i != list.size() - 1) {
                toReturn += "; ";
            }
        }

        return toReturn + ")";
    }

}
