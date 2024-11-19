package br.edu.utfpr.mapasatvrobison

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Redireciona para a MainActivity após 2 segundos
        Handler().postDelayed({
            val intent = Intent(this, Lista::class.java)
            startActivity(intent)
            finish() // Finaliza a SplashActivity para que o usuário não possa voltar a ela
        }, 2000) // 2000 ms = 2 segundos
    }
}