package at.aau.serg.cluedo

import MyStomp
import android.app.Application

class CluedoApp: Application() {
    //Global App managing "global" variables
    lateinit var mystomp: MyStomp
}