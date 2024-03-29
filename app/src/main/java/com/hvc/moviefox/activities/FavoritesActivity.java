package com.hvc.moviefox.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import com.hvc.moviefox.adapters.MovieRecyclerViewAdapter;
import com.hvc.moviefox.R;
import com.hvc.moviefox.data.FavoritesContract;
import com.hvc.moviefox.data.FavoritesDbHelper;
import com.hvc.moviefox.models.Movie;

/**
 * Favorites activity.
 * Used to show the user's favorite movies.
 */
public class FavoritesActivity extends AppCompatActivity {

    private SQLiteDatabase mDatabase;
    private RecyclerView rvMovieList;
    private ArrayList<Movie> movies;
    private TextView tvNoFavorites;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_trending:
                    Intent trendingIntent = new Intent(getApplicationContext(), TrendingActivity.class);
                    startActivity(trendingIntent);
                    return true;
                case R.id.navigation_search:
                    Intent searchIntent = new Intent(getApplicationContext(), SearchActivity.class);
                    startActivity(searchIntent);
                    return true;
                case R.id.navigation_favorites:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        rvMovieList = findViewById(R.id.rv_movie_list);
        rvMovieList.setNestedScrollingEnabled(false);

        // Set bottombar active item
        navigation.setSelectedItemId(R.id.navigation_favorites);

        // Create a database helper
        FavoritesDbHelper dbHelper = new FavoritesDbHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        // Get favorites and initialize movies
        Cursor cursorFavorites = getFavorites();
        initMovies(cursorFavorites);
        populateRecyclerView();

        // Hide text if there are favorite movies present
        tvNoFavorites = findViewById(R.id.tv_no_favorites);
        if (movies.size() > 0) {
            tvNoFavorites.setVisibility(View.GONE);
        }
    }

    /**
     * Called when the user switches back from movie detail activity to favorites activity.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Get favorites and initialize movies
        Cursor cursorFavorites = getFavorites();
        initMovies(cursorFavorites);
        populateRecyclerView();
    }

    /**
     * Get the favorites movies from de local database
     *
     * @return
     */
    private Cursor getFavorites() {
        return mDatabase.query(
                FavoritesContract.FavoritesEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                FavoritesContract.FavoritesEntry.COLUMN_TIMESTAMP + " DESC"
        );
    }

    /**
     * Populates the recyclerview with the movies defined in the movie array.
     */
    private void populateRecyclerView() {
        MovieRecyclerViewAdapter rvAdapter = new MovieRecyclerViewAdapter(getApplicationContext(), movies);

        // Decide the number of columns based on the screen width
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int spanCount = 2;
        if (width > 1400) {
            spanCount = 4;
        } else if (width > 700) {
            spanCount = 3;
        }

        rvMovieList.setLayoutManager(new GridLayoutManager(getApplicationContext(), spanCount));
        rvMovieList.setAdapter(rvAdapter);
    }

    /**
     * Reads the query result and initialize favorite movies into an array.
     *
     * @param cursorMovies
     */
    private void initMovies(Cursor cursorMovies) {
        try {
            movies = new ArrayList<>();
            while (cursorMovies.moveToNext()) {
                int movieId = cursorMovies.getInt(cursorMovies.getColumnIndex("movieId"));
                String posterPath = cursorMovies.getString(cursorMovies.getColumnIndex("posterPath"));
                movies.add(new Movie(movieId, posterPath));
            }
        } finally {
            cursorMovies.close();
        }
    }

}