package com.UniaccStressMonitor.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.UniaccStressMonitor.data.remote.AuthService
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, authService: AuthService) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF8F9FA), Color(0xFFE9ECEF))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                tonalElevation = 2.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Uniacc",
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "StressMonitor",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    errorMessage?.let {
                        Text(
                            text = translateError(it),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 12.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                val result = authService.login(email, password)
                                if (result.first) {
                                    onLoginSuccess()
                                } else {
                                    errorMessage = result.second ?: "UNKNOWN"
                                }
                                isLoading = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        else Text("Iniciar Sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = {
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            val result = authService.register(email, password)
                            if (result.first) {
                                onLoginSuccess()
                            } else {
                                errorMessage = result.second ?: "UNKNOWN"
                            }
                            isLoading = false
                        }
                    }) {
                        Text("Registrar Nueva Cuenta", color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

private fun translateError(error: String): String {
    val e = error.lowercase()
    return when {
        e.contains("badly formatted") || e.contains("invalid-email") -> 
            "El formato del correo electrónico no es válido."
        e.contains("user-not-found") || e.contains("no user record") -> 
            "No existe ninguna cuenta con este correo."
        e.contains("wrong-password") || e.contains("invalid credentials") || e.contains("invalid-credential") -> 
            "La contraseña es incorrecta o las credenciales no son válidas."
        e.contains("email-already-in-use") || e.contains("email already exists") -> 
            "Este correo electrónico ya está registrado por otro usuario."
        e.contains("weak-password") -> 
            "La contraseña es muy débil. Intenta con al menos 6 caracteres."
        e.contains("user-disabled") -> 
            "Esta cuenta de usuario ha sido deshabilitada."
        e.contains("too-many-requests") -> 
            "Demasiados intentos fallidos. Inténtalo más tarde."
        e.contains("network-request-failed") || e.contains("network error") -> 
            "Error de conexión. Revisa tu internet."
        e.contains("configuration_not_found") -> 
            "El servicio de autenticación no está configurado en Firebase."
        e.contains("unknown") ->
            "Ocurrió un error inesperado. Inténtalo de nuevo."
        else -> "Error: $error" // Mantiene el código original si no hay match, pero precedido de "Error"
    }
}
