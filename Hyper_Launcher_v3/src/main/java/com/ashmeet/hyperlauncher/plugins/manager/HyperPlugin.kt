package com.ashmeet.hyperlauncher.plugins.manager

import androidx.appcompat.app.AppCompatActivity

interface HyperPlugin {
    fun prepare(activity: AppCompatActivity, javaArgList: MutableList<String>, mcVersion: String)
}