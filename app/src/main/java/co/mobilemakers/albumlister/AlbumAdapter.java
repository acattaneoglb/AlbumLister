package co.mobilemakers.albumlister;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ariel.cattaneo on 13/02/2015.
 */
public class AlbumAdapter extends ArrayAdapter<Album> {
    List<Album> mAlbums;

    public class ViewHolder {
        public final TextView textViewTitle;
        public final TextView textViewLength;

        public ViewHolder(View view) {
            textViewTitle = (TextView)view.findViewById(R.id.text_view_album_title);
            textViewLength = (TextView)view.findViewById(R.id.text_view_album_length);
        }
    }

    public AlbumAdapter(Context context, List<Album> albums) {
        super(context, R.layout.list_item_album, albums);
        mAlbums = albums;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = reuseOrGenerateRowView(convertView, parent);

        displayAlbumInRow(position, rowView);

        return rowView;
    }

    private View reuseOrGenerateRowView(View convertView, ViewGroup parent) {
        View rowView;
        if (convertView != null) {
            rowView = convertView;
        }
        else {
            LayoutInflater inflater =
                    (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_item_album, parent, false);
            ViewHolder viewHolder = new ViewHolder(rowView);
            rowView.setTag(viewHolder);
        }
        return rowView;
    }

    private void displayAlbumInRow(int position, View rowView) {
        ViewHolder viewHolder = (ViewHolder) rowView.getTag();
        viewHolder.textViewTitle.setText(mAlbums.get(position).getTitle());
        viewHolder.textViewLength.setText(mAlbums.get(position).getLength());
    }
}
