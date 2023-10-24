package com.ozanyazici.artbookfragment.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.ozanyazici.artbookfragment.view.ArtFragmentDirections;
import com.ozanyazici.artbookfragment.databinding.RecyclerRowBinding;
import com.ozanyazici.artbookfragment.model.Art;

import java.util.List;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder> {

    List<Art> artList;
    public ArtAdapter(List<Art> artList) {

        this.artList = artList;
    }

    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerRowBinding binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ArtHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, int position) {
        holder.binding.rowTextView.setText(artList.get(position).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArtFragmentDirections.ActionArtFragmentToAddArtFragment action = ArtFragmentDirections.actionArtFragmentToAddArtFragment("old");
                action.setArtId(artList.get(holder.getAdapterPosition()).id);
                action.setInfo("old");
                Navigation.findNavController(view).navigate(action);

            }
        });
    }

    @Override
    public int getItemCount() {

        return artList.size();
    }

     public class ArtHolder extends RecyclerView.ViewHolder {

        private RecyclerRowBinding binding;

        public ArtHolder(@NonNull RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }
}
