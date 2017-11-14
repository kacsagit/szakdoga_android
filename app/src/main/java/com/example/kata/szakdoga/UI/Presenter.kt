package com.example.kata.szakdoga.UI

abstract class Presenter<S> {
    protected var screen: S? = null

    fun attachScreen(screen: S) {
        this.screen = screen
    }

    fun detachScreen() {
        this.screen = null
    }

}

