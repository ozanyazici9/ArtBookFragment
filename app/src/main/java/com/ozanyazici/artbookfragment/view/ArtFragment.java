package com.ozanyazici.artbookfragment.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ozanyazici.artbookfragment.adapter.ArtAdapter;
import com.ozanyazici.artbookfragment.databinding.FragmentArtBinding;
import com.ozanyazici.artbookfragment.model.Art;
import com.ozanyazici.artbookfragment.roomdb.ArtDao;
import com.ozanyazici.artbookfragment.roomdb.ArtDatabase;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class ArtFragment extends Fragment {

    private FragmentArtBinding binding;
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    ArtDatabase artDatabase;
    ArtDao artDao;
    ArtAdapter artAdapter;


    public ArtFragment() {
        // Required empty public constructor
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        artDatabase = Room.databaseBuilder(requireContext(),ArtDatabase.class,"Arts").build();

        artDao = artDatabase.artDao();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentArtBinding.inflate(inflater,container,false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerView.setLayoutManager(layoutManager);
        getData();
    }

    public void getData() {

        mDisposable.add(artDao.getArtWithNameAndId()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ArtFragment.this::handleResponse));

    }

    private void handleResponse(List<Art> artList) {

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        artAdapter = new ArtAdapter(artList);
        binding.recyclerView.setAdapter(artAdapter);




    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mDisposable.clear();

        // binding nesnesini null olarak ayarlamanın nedeni, Fragment'ın onDestroyView yöntemi çağrıldığında,
        // Fragment'ın bağlı olduğu View'un bellekten serbest bırakılması gerektiğini belirtmektir.
        // Bu, Fragment'ın View nesnesi artık kullanılmayacağı ve bellekten silineceği anlamına gelir.
    }
}