package com.example.photogallery.api

import com.example.photogallery.GalleryItem
import com.google.gson.annotations.SerializedName

class PhotoResponse {
    @SerializedName("photo")
    var galleryItems: List<GalleryItem> = emptyList()
}