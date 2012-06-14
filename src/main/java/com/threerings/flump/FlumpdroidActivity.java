//
// Flump - Copyright 2012 Three Rings Design

package com.threerings.flump;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import flump.display.Movie;
import flump.display.MovieMaker;
import flump.mold.LibraryMold;
import flump.mold.MovieMold;
import flump.simplexml.FlumpParser;

public class FlumpdroidActivity extends Activity
{
    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        AssetManager assets = getResources().getAssets();
        _flumpParser = new FlumpParser(assets, "main");

        setContentView(R.layout.main);

        _renderer = ((FlumpView)findViewById(R.id.flump_view)).getRenderer();
        _librarySpinner = (Spinner)findViewById(R.id.library_spinner);
        _movieSpinner = (Spinner)findViewById(R.id.movie_spinner);

        try {
            // Library gets all the things we know about
            final String[] movies = assets.list(_flumpParser.getBasePath());
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, movies);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            _librarySpinner.setAdapter(adapter);
            _librarySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected (
                    AdapterView<?> parent, View view, int position, long id) {
                    setLibrary(movies[position]);
                }

                @Override
                public void onNothingSelected (AdapterView<?> parent) {
                    // Ack?
                }
            });

            // And then pick one
            _librarySpinner.setSelection(0);

            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
            _movieSpinner.setAdapter(adapter);
            _movieSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected (
                    AdapterView<?> parent, View view, int position, long id) {

                    setMovie((String)_movieSpinner.getAdapter().getItem(position));
                }

                @Override
                public void onNothingSelected (AdapterView<?> parent) {
                    // Ack?
                }
            });

        } catch (Exception e) {
            // OH NOES
        }
    }

    protected void setLibrary (String library)
    {
        try {
            _library = _flumpParser.loadLibrary(library);
            _maker = new MovieMaker(_library);
        } catch (Exception e) {
            // OH NOES
        }

        @SuppressWarnings("unchecked")
        ArrayAdapter<String> adapter = (ArrayAdapter<String>)_movieSpinner.getAdapter();

        adapter.clear();
        for (MovieMold movie : _library.movies) {
            adapter.add(movie.id);
        }

        _movieSpinner.setSelection(0);
        // HACK HACK! We might be on that number already, in which case the signal won't get fired
        setMovie(_library.movies.get(0).id);
    }

    protected void setMovie (String id)
    {
        // Throw away everything on the view
        _renderer.clearDisplayObjects();

        // And add the new guy
        _renderer.addDisplayObject(((Movie)_maker.getDisplayObject(id)).loop());
        _renderer.resetScale();
    }

    protected FlumpParser _flumpParser;
    protected Spinner _librarySpinner;
    protected Spinner _movieSpinner;
    protected FlumpRenderer _renderer;

    protected LibraryMold _library;
    protected MovieMaker _maker;
}
