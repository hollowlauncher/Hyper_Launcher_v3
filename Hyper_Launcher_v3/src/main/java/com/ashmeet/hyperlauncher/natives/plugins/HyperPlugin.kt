package com.ashmeet.hyperlauncher.natives.plugins

import androidx.appcompat.app.AppCompatActivity

interface HyperPlugin {
    fun prepare(activity: AppCompatActivity, javaArgList: MutableList<String>, mcVersion: String)
}
