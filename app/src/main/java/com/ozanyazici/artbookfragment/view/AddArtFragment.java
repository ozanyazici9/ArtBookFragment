package com.ozanyazici.artbookfragment.view;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.room.Room;

import com.google.android.material.snackbar.Snackbar;
import com.ozanyazici.artbookfragment.R;
import com.ozanyazici.artbookfragment.databinding.FragmentAddArtBinding;
import com.ozanyazici.artbookfragment.model.Art;
import com.ozanyazici.artbookfragment.roomdb.ArtDao;
import com.ozanyazici.artbookfragment.roomdb.ArtDatabase;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class AddArtFragment extends Fragment {

    ArtDatabase artDatabase;
    ArtDao artDao;
    String info = "";
    ActivityResultLauncher<String> permissionLauncher;
    ActivityResultLauncher<Intent> activityResultLauncher;
    Art artFromMain;
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private FragmentAddArtBinding binding;
    Bitmap selectedImage;

    public AddArtFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        artDatabase = Room.databaseBuilder(requireContext(), ArtDatabase.class,"Arts").build();

        artDao = artDatabase.artDao();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddArtBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        registerLauncher();


        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(view);
            }
        });


        binding.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save(view);
            }
        });

        binding.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete(view);
            }
        });


        if(getArguments() != null) {
            info = AddArtFragmentArgs.fromBundle(getArguments()).getInfo();
        } else {
            info = "new";
        }

        if(info.equals("new")) {
            binding.nameText.setText("");
            binding.artistText.setText("");
            binding.yearText.setText("");
            binding.deleteButton.setVisibility(View.GONE);
            binding.saveButton.setVisibility(View.VISIBLE);

            binding.imageView.setImageResource(R.drawable.selectimage);

        } else {
            int artId = AddArtFragmentArgs.fromBundle(getArguments()).getArtId();
            binding.saveButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);

            mDisposable.add(artDao.getArtById(artId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(AddArtFragment.this::handleResponseWithOldArt));
        }
    }

    private void handleResponseWithOldArt(Art art) {
        artFromMain = art;
        binding.nameText.setText(art.name);
        binding.artistText.setText(art.artistName);
        binding.yearText.setText(art.year);

        Bitmap bitmap = BitmapFactory.decodeByteArray(art.image,0,art.image.length);
        binding.imageView.setImageBitmap(bitmap);
    }

    public void selectImage(View view) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_LONG).setAction("Give permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }
            } else {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        } else {
            if(ContextCompat.checkSelfPermission(requireContext(),Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view,"Permission needed for Gallery",Snackbar.LENGTH_LONG).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            } else {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }

    }

    public void save(View view) {

        String artName = binding.nameText.getText().toString();
        String artistName = binding.artistText.getText().toString();
        String artYear = binding.yearText.getText().toString();

        Bitmap smallImage = makeSmallerImage(selectedImage,300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,75,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        Art art = new Art(artName,artistName,artYear,byteArray);

        mDisposable.add(artDao.insert(art)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AddArtFragment.this::handleResponse));

    }

    public void delete(View view) {
        mDisposable.add(artDao.delete(artFromMain)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(AddArtFragment.this::handleResponse));



    }

    //requireView() bir Android Fragment sınıfı içinde kullanılan bir işlevdir.
    //Bu işlev, Fragment'in bağlı olduğu View öğesini
    //(genellikle bir fragment'in kullanıcı arayüzünü tanımlayan bir layout) almak için kullanılır.

    public void handleResponse() {
        NavDirections action = AddArtFragmentDirections.actionAddArtFragmentToArtFragment();
        Navigation.findNavController(requireView()).navigate(action);
    }

    /*activityResultLauncher bir ActivityResultLauncher nesnesidir
    ve ActivityResultContracts.StartActivityForResult() ile ilişkilendirilmiştir.
    Bu, başka bir Activity başlatma işlemi için bir sözleşmeyi temsil eder.
    Dolayısıyla, activityResultLauncher.launch(intentToGallery) ifadesi, intentToGallery adlı Intent'i başlatır
    ve kullanıcının bu Intent ile başlatılan işlemi tamamlamasının ardından sonucu işlemek için ActivityResultCallback'i çağırır.
    ActivityResultCallback<ActivityResult> arayüzü, başka bir Activity'den dönen sonuçları işlemek için kullanılır.

    requireActivity() metodu, bir Fragment içinde çağrıldığında, bu Fragment'in bağlı olduğu Activity'ye bir referans döndürür.
    Bu referansı kullanarak Fragment, bağlı olduğu Activity ile iletişim kurabilir,
    Activity içindeki verilere veya yöntemlere erişebilir.
     */

    public void registerLauncher() {
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent intentFromResult = result.getData();
                            if (intentFromResult != null) {
                                Uri imageData = intentFromResult.getData();
                                try {

                                    if(Build.VERSION.SDK_INT >= 28) {
                                        ImageDecoder.Source source = ImageDecoder.createSource(requireActivity().getContentResolver(),imageData);
                                        selectedImage = ImageDecoder.decodeBitmap(source);
                                        binding.imageView.setImageBitmap(selectedImage);

                                    } else {
                                        selectedImage = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(),imageData);
                                        binding.imageView.setImageBitmap(selectedImage);

                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }
                }
        );

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {

            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);

                } else {
                    Toast.makeText(requireActivity(),"Permission needed!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public Bitmap makeSmallerImage(Bitmap image, int maximumSize){

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if(bitmapRatio > 1){
            width = maximumSize;
            height = (int) (width / bitmapRatio);

        } else {
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image,width,height,true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mDisposable.clear();
    }
}