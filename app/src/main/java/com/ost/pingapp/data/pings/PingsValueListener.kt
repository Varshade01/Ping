package com.khrd.pingapp.data.pings

import com.google.firebase.database.ValueEventListener

abstract class PingsValueListener(val offset: String) : ValueEventListener {
    override fun equals(other: Any?): Boolean {
        return other is PingsValueListener && offset == other.offset
    }

    override fun hashCode(): Int {
        return offset.hashCode()
    }
}