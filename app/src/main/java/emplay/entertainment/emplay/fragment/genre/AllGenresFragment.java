package emplay.entertainment.emplay.fragment.genre;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import emplay.entertainment.emplay.R;
import emplay.entertainment.emplay.adapter.common.GenresAdapter;
import emplay.entertainment.emplay.fragment.common.BaseFragment;
import emplay.entertainment.emplay.models.common.GenresModel;

public class AllGenresFragment extends BaseFragment {

    private static final String ARG_GENRE_IDS   = "GENRE_IDS";
    private static final String ARG_GENRE_NAMES = "GENRE_NAMES";
    private static final String ARG_IS_TV        = "IS_TV";

    public static AllGenresFragment newInstance(List<GenresModel> genres, boolean isTv) {
        int[] ids = new int[genres.size()];
        String[] names = new String[genres.size()];
        for (int i = 0; i < genres.size(); i++) {
            ids[i] = genres.get(i).getId();
            names[i] = genres.get(i).getName();
        }
        Bundle args = new Bundle();
        args.putIntArray(ARG_GENRE_IDS, ids);
        args.putStringArray(ARG_GENRE_NAMES, names);
        args.putBoolean(ARG_IS_TV, isTv);
        AllGenresFragment f = new AllGenresFragment();
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_all_genres, container, false);

        Bundle args = requireArguments();
        int[] ids = args.getIntArray(ARG_GENRE_IDS);
        String[] names = args.getStringArray(ARG_GENRE_NAMES);
        boolean isTv = args.getBoolean(ARG_IS_TV);

        List<GenresModel> genres = new ArrayList<>();
        if (ids != null && names != null) {
            for (int i = 0; i < ids.length; i++) {
                genres.add(new GenresModel(ids[i], names[i], 0));
            }
        }

        GenresAdapter adapter = new GenresAdapter(genres, genre -> {
            if (isTv) {
                navigateTo(TVShowsByGenresFragment.newInstance(genre.getId(), genre.getName()));
            } else {
                navigateTo(MovieByGenresFragment.newInstance(genre.getId(), genre.getName()));
            }
        });

        RecyclerView rv = view.findViewById(R.id.rvAllGenres);
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rv.setAdapter(adapter);

        return view;
    }
}