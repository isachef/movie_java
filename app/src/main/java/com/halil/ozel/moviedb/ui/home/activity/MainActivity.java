package com.halil.ozel.moviedb.ui.home.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halil.ozel.moviedb.App;
import com.halil.ozel.moviedb.R;
import com.halil.ozel.moviedb.data.Api.TMDbAPI;
import com.halil.ozel.moviedb.data.models.Results;
import com.halil.ozel.moviedb.ui.home.adapters.MovieAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.halil.ozel.moviedb.data.Api.TMDbAPI.TMDb_API_KEY;

public class MainActivity extends AppCompatActivity {

    @Inject
    TMDbAPI tmDbAPI;

    private RecyclerView rvPopularMovie;
    private MovieAdapter popularMovieAdapter;
    private List<Results> popularMovieDataList;
    private List<Results> allPopularMovies; // Полный список популярных фильмов

    private RecyclerView rvNowPlaying;
    private MovieAdapter nowPlayingMovieAdapter;
    private List<Results> nowPlayingDataList;
    private List<Results> allNowPlayingMovies; // Полный список текущих фильмов

    private EditText etSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.instance().appComponent().inject(this);
        setContentView(R.layout.activity_main);

        popularMovieDataList = new ArrayList<>();
        allPopularMovies = new ArrayList<>();
        nowPlayingDataList = new ArrayList<>();
        allNowPlayingMovies = new ArrayList<>();

        setupRecyclerViews();
        etSearch = findViewById(R.id.etSearch);

        // Добавление слушателя для поиска
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMovies(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Загрузка данных из API
        getPopularMovies();
        getNowPlaying();
    }

    private void setupRecyclerViews() {
        // RecyclerView для популярных фильмов
        rvPopularMovie = findViewById(R.id.rvPopularMovie);
        popularMovieAdapter = new MovieAdapter(popularMovieDataList, this);
        rvPopularMovie.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvPopularMovie.setAdapter(popularMovieAdapter);

        // RecyclerView для текущих фильмов
        rvNowPlaying = findViewById(R.id.rvNowPlaying);
        nowPlayingMovieAdapter = new MovieAdapter(nowPlayingDataList, this);
        rvNowPlaying.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvNowPlaying.setAdapter(nowPlayingMovieAdapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterMovies(String query) {

        nowPlayingDataList.clear();
        if (query.isEmpty()) {
            nowPlayingDataList.addAll(allNowPlayingMovies);
        } else {
            for (Results movie : allNowPlayingMovies) {
                if (movie.getTitle().toLowerCase().contains(query)) {
                    nowPlayingDataList.add(movie);
                }
            }
        }
        nowPlayingMovieAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void getNowPlaying() {
        tmDbAPI.getNowPlaying(TMDb_API_KEY, 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    allNowPlayingMovies.clear();
                    allNowPlayingMovies.addAll(response.getResults()); // Сохраняем полный список
                    nowPlayingDataList.clear();
                    nowPlayingDataList.addAll(allNowPlayingMovies); // Отображаемые данные
                    nowPlayingMovieAdapter.notifyDataSetChanged();
                }, e -> Timber.e(e, "Error fetching now playing movies: %s", e.getMessage()));
    }

    @SuppressLint("NotifyDataSetChanged")
    public void getPopularMovies() {
        tmDbAPI.getPopularMovie(TMDb_API_KEY, 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    allPopularMovies.clear();
                    allPopularMovies.addAll(response.getResults()); // Сохраняем полный список
                    popularMovieDataList.clear();
                    popularMovieDataList.addAll(allPopularMovies); // Отображаемые данные
                    popularMovieAdapter.notifyDataSetChanged();
                }, e -> Timber.e(e, "Error fetching popular movies: %s", e.getMessage()));
    }
}