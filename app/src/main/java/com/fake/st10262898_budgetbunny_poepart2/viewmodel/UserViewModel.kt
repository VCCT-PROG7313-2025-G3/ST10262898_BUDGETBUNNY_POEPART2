import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val registrationResult = MutableLiveData<Boolean>()
    val loginResult = MutableLiveData<Boolean>()

    // Helper function to convert username to fake email (This is because firestore needs an email)
    private fun usernameToEmail(username: String) = "$username@myapp.fake"

    //Allows users to be registred into firestore:
    fun registerUser(username: String, password: String) {
        val email = usernameToEmail(username)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                registrationResult.postValue(task.isSuccessful)
            }
    }

    //allows users to login into application
    fun loginUser(username: String, password: String) {
        val email = usernameToEmail(username)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                loginResult.postValue(task.isSuccessful)
            }
    }

    //Allows users to logout
    fun logoutUser() {
        auth.signOut()
    }

    //Gets teh current user that is currently logged on
    fun getCurrentUser() = auth.currentUser
}
