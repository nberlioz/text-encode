package com.example.cryptoapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CryptoScreen()
                }
            }
        }
    }
}

@Composable
fun CryptoScreen() {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var passphrase by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Chiffrement Symétrique AES-GCM",
            style = MaterialTheme.typography.headlineSmall
        )

        // Champ pour le texte source ou le texte chiffré
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Texte à chiffrer ou déchiffrer") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        // Champ pour la clé secrète
        OutlinedTextField(
            value = passphrase,
            onValueChange = { passphrase = it },
            label = { Text("Clé secrète / Mot de passe") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        // Boutons d'action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (inputText.isNotBlank() && passphrase.isNotBlank()) {
                        try {
                            resultText = CryptoManager.encrypt(inputText, passphrase)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erreur de chiffrement", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Chiffrer")
            }

            Button(
                onClick = {
                    if (inputText.isNotBlank() && passphrase.isNotBlank()) {
                        try {
                            resultText = CryptoManager.decrypt(inputText, passphrase)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erreur ou clé incorrecte !", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Déchiffrer")
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Résultat
        Text(text = "Résultat :", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = resultText,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}
