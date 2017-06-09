package pe.net.lambda.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gary on 5/06/2017.
 */

public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";
    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();

    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance(){
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemTask().execute();

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloaderListener(
                new ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder>(){
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap){
                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                        photoHolder.bindDrawable(drawable);
                    }
                }

        );

        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance){
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        setupAdapter();

        return v;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    private class FetchItemTask extends AsyncTask<Void, Void, List<GalleryItem>>{

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            /*try{
                String result = new FlickFetchr().getUrlString("https://www.bignerdranch.com");
                Log.i(TAG, "Fetched contents of URL: "+result);
            }catch(IOException ioe){
                Log.e(TAG, "Failed to fetch URL: ", ioe);
            }*/
           return new FlickFetchr().fetchItems();
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items){
            mItems = items;
            setupAdapter();
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder{

        private ImageView mItemImageView;

        public PhotoHolder(View itemView) {
            super(itemView);
//            mTitleTextView = (TextView)itemView;
            mItemImageView = (ImageView)itemView.findViewById(R.id.fragment_photo_gallery_image_view);
        }

        public void bindDrawable(Drawable drawable){
            mItemImageView.setImageDrawable(drawable);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{

        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems){
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            /*TextView textView = new TextView(getActivity());
            return new PhotoHolder(textView);*/
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
//            holder.bindGalleryItem(galleryItem);
            Drawable placeHolder = getResources().getDrawable(R.drawable.bill_up_close);
            holder.bindDrawable(placeHolder);
            mThumbnailDownloader.queueThumbnail(holder, galleryItem.getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private void setupAdapter(){
        if(isAdded()){
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

}
