package co.mobilemakers.albumlister;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class AlbumListerFragment extends ListFragment {

    final static String LOG_TAG = AlbumListerFragment.class.getSimpleName();

    EditText mEditTextArtist;
    AlbumAdapter mAdapter;

    protected class ArtistCallback implements Callback {
        @Override
        public void onFailure(Request request, IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(Response response) throws IOException {
            String responseString = response.body().string();

            final String id = parseArtistResponse(responseString);

            try {
                URL url = constructURLAlbumsQuery(id);

                Request idRequest = new Request.Builder().url(url.toString()).build();
                OkHttpClient client = new OkHttpClient();
                client.newCall(idRequest).enqueue(new AlbumCallback());
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected class AlbumCallback implements Callback {
        @Override
        public void onFailure(Request request, IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(Response response) throws IOException {
            String responseString = response.body().string();

            final List<Album> listOfAlbums = parseAlbumResponse(responseString);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.clear();
                    mAdapter.addAll(listOfAlbums);
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    public AlbumListerFragment() {
    }

    private void prepareListAdapter() {
        List<Album> albums = new ArrayList<>();
        mAdapter = new AlbumAdapter(getActivity(), albums);
        setListAdapter(mAdapter);
    }

    private void prepareButtons(View rootView) {
        Button buttonGetAlbums = (Button)rootView.findViewById(R.id.button_get_albums);
        buttonGetAlbums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String artist = mEditTextArtist.getText().toString();
                displayToast(artist);

                fetchAlbumsFromQueue(artist);
            }

            private void displayToast(String artist) {
                String message = String.format(getString(R.string.getting_artist_albums), artist);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void wireUpViews(View rootView) {
        mEditTextArtist = (EditText)rootView.findViewById(R.id.edit_text_artist);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_album_lister, container, false);

        wireUpViews(rootView);
        prepareButtons(rootView);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prepareListAdapter();
    }

    private void fetchAlbumsFromQueue(String artist) {
        try {
            URL url = constructURLArtistQuery(artist);

            Request idRequest = new Request.Builder().url(url.toString()).build();
            OkHttpClient client = new OkHttpClient();
            client.newCall(idRequest).enqueue(new ArtistCallback());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private URL constructURLArtistQuery(String artist) throws MalformedURLException {
        final String MUSICBRAINZ_BASE_URL = "musicbrainz.org";
        final String API_PATH_1 = "ws";
        final String API_PATH_2 = "2";
        final String ARTIST_ENDPOINT = "artist";
        final String QUERY_KEY = "query";

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http").authority(MUSICBRAINZ_BASE_URL)
                .appendPath(API_PATH_1)
                .appendPath(API_PATH_2)
                .appendPath(ARTIST_ENDPOINT)
                .appendQueryParameter(QUERY_KEY, artist)
                .appendQueryParameter("fmt", "json");
        Uri uri = builder.build();
        Log.d(LOG_TAG, "Built URI: " + uri.toString());

        return new URL(uri.toString());
    }

    private URL constructURLAlbumsQuery(String artistId) throws MalformedURLException {
        final String MUSICBRAINZ_BASE_URL = "musicbrainz.org";
        final String API_PATH_1 = "ws";
        final String API_PATH_2 = "2";
        final String ARTIST_ENDPOINT = "artist";
        final String INC_KEY = "inc";
        final String RECORDINGS_PARAMETER = "recordings";

        // Those are themes, not albums
        // TODO: See https://musicbrainz.org/doc/Development/XML_Web_Service/Version_2 (release or release-group)

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http").authority(MUSICBRAINZ_BASE_URL)
                .appendPath(API_PATH_1)
                .appendPath(API_PATH_2)
                .appendPath(ARTIST_ENDPOINT)
                .appendPath(artistId)
                .appendQueryParameter(INC_KEY, RECORDINGS_PARAMETER)
                .appendQueryParameter("fmt", "json");
        Uri uri = builder.build();
        Log.d(LOG_TAG, "Built URI: " + uri.toString());

        return new URL(uri.toString());
    }

    private String parseArtistResponse(String response) {
        final String ARTISTS_ARRAY = "artists";
        final String ARTIST_ID = "id";

        String id = "";

        try {
            JSONObject responseJsonObject = new JSONObject(response);
            JSONArray responseJsonArray = responseJsonObject.getJSONArray(ARTISTS_ARRAY);
            JSONObject object;
            object = responseJsonArray.getJSONObject(0);
            id = object.getString(ARTIST_ID);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return id;
    }

    private List<Album> parseAlbumResponse(String response) {
        final String ARTIST_RECS = "recordings";
        final String REC_TITLE = "title";
        final String REC_LENGTH = "length";

        List<Album> albums = new ArrayList<>();
        Album album;
        try {
            JSONObject artistJson = new JSONObject(response);
            JSONArray responseJsonArray = artistJson.getJSONArray(ARTIST_RECS);
            JSONObject object;
            for (int i = 0; i < responseJsonArray.length(); i++) {
                object = responseJsonArray.getJSONObject(i);
                album = new Album();
                album.setTitle(object.getString(REC_TITLE));
                album.setLength(object.getString(REC_LENGTH));
                albums.add(album);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return albums;
    }
}
