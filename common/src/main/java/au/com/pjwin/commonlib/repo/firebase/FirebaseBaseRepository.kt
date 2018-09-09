package au.com.pjwin.commonlib.repo.firebase

import com.google.firebase.database.FirebaseDatabase

abstract class FirebaseBaseRepository {

    protected val database = FirebaseDatabase.getInstance()
    init {
        database.setPersistenceEnabled(true)
    }

    protected val rootReference = database.reference
}