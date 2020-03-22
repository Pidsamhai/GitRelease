package com.github.pidsamhai.gitrelease

import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.github.kittinunf.fuel.coroutines.awaitObjectResponse
import com.github.kittinunf.fuel.gson.gsonDeserializerOf
import com.github.kittinunf.fuel.httpGet
import com.github.pidsamhai.gitrelease.response.ReleaseResponse
import kotlin.Exception
import com.github.pidsamhai.gitrelease.response.GitReleaseResponse
import com.github.pidsamhai.gitrelease.response.ResponseData

class GitRelease() {

    private val baseUrl:String = "https://api.github.com/repos"
    var owner:String? = null
    var repo:String? = null
    var currentVersion:String? = null
    private var isNew:Boolean = false


    private fun fullUrl():String {
        val fullUrl = "$baseUrl/$owner/$repo/releases"
        Log.i(TAG, "fullUrl: $fullUrl")
        return fullUrl
    }

    suspend fun getRelease():ReleaseResponse{
        return try {
            val(_,_,resObj) = fullUrl()
                .httpGet()
                    .awaitObjectResponse(gsonDeserializerOf(Array<GitReleaseResponse>::class.java))
            Log.i(TAG, "getRelease: $resObj")
            if(currentVersion != resObj[0].tagName){
                isNew = true
            }
            val data = ResponseData(resObj,null)
            ReleaseResponse(data,false,isNew)
        }catch (e : Exception){
            Log.e(TAG, "getRelease: ${e.message}",e)
            val data = ResponseData(null,e)
            ReleaseResponse(data,true,isNew)
        }
    }

}