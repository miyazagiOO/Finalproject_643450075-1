package th.ac.kku.cis.finalproject

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.firestore.FirebaseFirestore
import th.ac.kku.cis.finalproject.ui.theme.FinalprojectTheme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinalprojectTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "LetWriteScreen") {
                    composable("LetWriteScreen") {
                        LetWriteScreen(navController)
                    }
                    composable("displayScreen") {
                        DisplayScreen(navController)
                    }
                    composable("noteScreen") {
                        NoteScreen(navController)
                    }
                    // Add the EditScreen composable
                    composable(
                        route = "EditScreen/{index}",
                        arguments = listOf(navArgument("index") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val index = backStackEntry.arguments?.getInt("index") ?: 0
                        EditScreen(navController, index)
                    }
                }
            }
        }

    }
}
@Composable
fun DisplayScreen(navController: NavHostController) {
    var noteList by remember { mutableStateOf<List<String>>(emptyList()) }
    var descriptionList by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Notes")
            .addSnapshotListener { snapshot, _ ->
                val notes = snapshot?.documents?.mapNotNull { it.getString("note") }
                notes?.let { noteList = it }
                val descriptions = snapshot?.documents?.mapNotNull { it.getString("description") }
                descriptions?.let { descriptionList = it }
            }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFD5FFFC), // กำหนดสีพื้นหลังเป็น #D5FFFC
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // แสดงข้อมูลที่ดึงมาจาก Firebase Firestore
            items(noteList.size) { index ->
//                Image(
//                    painter = painterResource(id = R.drawable.star1), // เพิ่มรูปภาพตามต้องการ
//                    contentDescription = null,
//                    modifier = Modifier.size(200.dp), // กำหนดขนาดของรูป
//                )
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(350.dp)
                        .height(200.dp)
                        .border(1.dp, Color.Black)
                        .background(Color.White) // เปลี่ยนสีพื้นหลังเป็นสีขาว
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = "Note: ${noteList[index]}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Description: ${descriptionList.getOrElse(index) { "" }}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    // ส่งไปยังหน้าแก้ไขโดยส่ง index ไปด้วย
                                    navController.navigate("EditScreen/$index")
                                },
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(width = 90.dp, height = 60.dp), // กำหนดขนาดของปุ่ม
                                colors = ButtonDefaults.buttonColors(Color(0xFFF6F42B))
                            ) {
                                Text("Edit")
                            }
                            Button(
                                onClick = {
                                    deleteDataFromFirestore(index)
                                },
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(width = 90.dp, height = 60.dp), // กำหนดขนาดของปุ่ม
                                colors = ButtonDefaults.buttonColors(Color(0xFFF62B2B))

                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }

            // ปุ่ม Create สำหรับเปลี่ยนหน้าไปยัง NoteScreen
            item {
                Button(
                    onClick = {
                        navController.navigate("noteScreen") // Navigate to the note screen
                    },
                    modifier = Modifier
                        .padding(10.dp)
                        .size(width = 200.dp, height = 60.dp) // กำหนดขนาดของปุ่ม
                ) {
                    Text("Create", fontSize = 20.sp) // กำหนดขนาดตัวอักษรของปุ่ม
                }
            }

        }
    }
}


fun deleteDataFromFirestore(index: Int) {
    val db = FirebaseFirestore.getInstance()
    db.collection("Notes")
        .get()
        .addOnSuccessListener { result ->
            val document = result.documents[index]
            document.reference.delete()
                .addOnSuccessListener {
                    Log.d("DisplayScreen", "DocumentSnapshot successfully deleted!")
                }
                .addOnFailureListener { e ->
                    Log.w("DisplayScreen", "Error deleting document", e)
                }
        }
        .addOnFailureListener { exception ->
            Log.w("DisplayScreen", "Error getting documents.", exception)
        }
}

@Composable
fun NoteScreen(navController: NavHostController) {
    var note by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFD5FFFC), // กำหนดสีพื้นหลังเป็น #D5FFFC
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note") },
                modifier = Modifier.padding(8.dp)
            )

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.padding(8.dp)
            )

            Button(
                onClick = {
                    // ทำการบันทึกข้อมูลที่ใส่เข้าไปใน Firebase Firestore
                    saveDataToFirestore(note, description)
                    // เด้งไปยังหน้า displayScreen
                    navController.navigate("displayScreen")
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Save")
            }

        }
    }
}
@Composable
fun LetWriteScreen(navController: NavHostController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFD5FFFC), // กำหนดสีพื้นหลังเป็น #D5FFFC
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.sstar), // เพิ่มรูปภาพตามต้องการ
                contentDescription = null,
                modifier = Modifier.size(500.dp), // กำหนดขนาดของรูป
            )

            // ปุ่มเพื่อเปลี่ยนหน้าไปยัง DisplayScreen
            Button(
                onClick = {
                    navController.navigate("displayScreen")
                },
                modifier = Modifier
                    .padding(10.dp)
                    .size(width = 200.dp, height = 60.dp), // กำหนดขนาดของปุ่ม
                colors = ButtonDefaults.buttonColors(Color(0xFFFFD0CB)) // กำหนดสีของปุ่ม
            ) {
                Text("Let's write your note!", fontSize = 15.sp) // กำหนดขนาดตัวอักษรของปุ่ม
            }
        }
    }
}
@Composable
fun EditScreen(navController: NavHostController, index: Int) {
    var editedNote by remember { mutableStateOf("") }
    var editedDescription by remember { mutableStateOf("") }

    // Function to fetch data from Firestore
    fun fetchDataFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Notes")
            .get()
            .addOnSuccessListener { result ->
                val document = result.documents[index]
                editedNote = document.getString("note") ?: ""
                editedDescription = document.getString("description") ?: ""
            }
            .addOnFailureListener { exception ->
                Log.w("EditScreen", "Error getting documents.", exception)
            }
    }

    // Fetch data from Firestore when the screen is first composed
    LaunchedEffect(Unit) {
        fetchDataFromFirestore()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFD5FFFC), // Set background color
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = editedNote,
                onValueChange = { editedNote = it },
                label = { Text("Note") },
                modifier = Modifier.padding(8.dp)
            )

            TextField(
                value = editedDescription,
                onValueChange = { editedDescription = it },
                label = { Text("Description") },
                modifier = Modifier.padding(8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        // Update data in Firestore
                        updateDataInFirestore(index, editedNote, editedDescription)
                        // Navigate back to the DisplayScreen
                        navController.popBackStack()
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Confirm")
                }

                Button(
                    onClick = {
                        // Navigate back to the DisplayScreen without making any changes
                        navController.popBackStack()
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

fun updateDataInFirestore(index: Int, editedNote: String, editedDescription: String) {
    val db = FirebaseFirestore.getInstance()
    db.collection("Notes")
        .get()
        .addOnSuccessListener { result ->
            val document = result.documents[index]
            document.reference.update("note", editedNote, "description", editedDescription)
                .addOnSuccessListener {
                    Log.d("EditScreen", "DocumentSnapshot successfully updated!")
                }
                .addOnFailureListener { e ->
                    Log.w("EditScreen", "Error updating document", e)
                }
        }
        .addOnFailureListener { exception ->
            Log.w("EditScreen", "Error getting documents.", exception)
        }
}


fun saveDataToFirestore(note: String, description: String) {
    val db = FirebaseFirestore.getInstance()
    val data = hashMapOf(
        "note" to note,
        "description" to description
    )
    db.collection("Notes") // เปลี่ยนเป็นชื่อของ collection ใน Firestore ของคุณ
        .add(data)
        .addOnSuccessListener { documentReference ->
            Log.d("MainActivity", "DocumentSnapshot added with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.w("MainActivity", "Error adding document", e)
        }
}
