package com.ashmeet.hyperlauncher.plugins.interfaces

import androidx.appcompat.app.AppCompatActivity

interface HyperPlugin {
    fun prepare(activity: AppCompatActivity, javaArgList: MutableList<String>, mcVersion: String)
}