package at.aau.serg.cluedo

import MyStomp
import android.app.Application

class CluedoApp: Application() {
    lateinit var mystomp: MyStomp
}