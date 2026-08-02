package com.example.cryptoapp

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
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
    
    // État pour stocker la Bitmap du QR Code
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Permet le défilement si le QR Code prend de la place
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
                            // 1. Chiffrement
                            val encrypted = CryptoManager.encrypt(inputText, passphrase)
                            resultText = encrypted
                            
                            // 2. Génération du QR Code
                            qrBitmap = CryptoManager.generateQrCode(encrypted, 500)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erreur de chiffrement", Toast.LENGTH_SHORT).show()
                            qrBitmap = null
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
                            // Déchiffrement et réinitialisation du QR Code
                            resultText = CryptoManager.decrypt(inputText, passphrase)
                            qrBitmap = null 
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

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Résultat texte
        Text(text = "Résultat :", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = resultText,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        // Affichage du QR Code s'il a été généré
        qrBitmap?.let { bitmap ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "QR Code chiffré :",
                    style = MaterialTheme.typography.titleMedium
                )
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "QR Code du texte chiffré",
                    modifier = Modifier.size(250.dp)
                )
            }
        }
    }
}