package dev.adriele.adolescare.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import dev.adriele.adolescare.R
import dev.adriele.adolescare.adapter.CreditsAdapter
import dev.adriele.adolescare.adapter.DevelopersAdapter
import dev.adriele.adolescare.databinding.ActivityAboutUsBinding
import dev.adriele.adolescare.model.Credits
import dev.adriele.adolescare.model.Developers

class AboutUsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutUsBinding
    private lateinit var developersAdapter: DevelopersAdapter
    private lateinit var creditsAdapter: CreditsAdapter
    private var developers = mutableListOf<Developers>()
    private var credits = mutableListOf<Credits>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAboutUsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setModels()
        initAdapters()
    }

    private fun setModels() {

        developers.addAll(
            listOf(
                Developers(
                    getString(R.string.criselda_d_babas),
                    getString(R.string.research_leader_programmer),
                    R.drawable.dev_1
                ),
                Developers(
                    getString(R.string.eda_jenn_o_gonzales),
                    getString(R.string.animator_editor),
                    R.drawable.dev_2
                ),
                Developers(
                    getString(R.string.betuel_c_alerta),
                    getString(R.string.researcher),
                    R.drawable.dev_3
                ),
                Developers(
                    getString(R.string.rebecca_a_bagalihog),
                    getString(R.string.researcher),
                    R.drawable.dev_4
                )
            )
        )

        credits.addAll(
            listOf(
                Credits(
                    getString(R.string.eden_a_gabutera),
                    getString(R.string.nurse_iii),
                    R.drawable.credit_1
                ),
                Credits(
                    getString(R.string.jenilyn_figueroa_lomocso_md),
                    getString(R.string.municipal_health_officer),
                    R.drawable.credit_2
                ),
                Credits(
                    getString(R.string.genesis_a_ysibido_rn_rm_man),
                    getString(R.string.faculty_midwifery_department_san_jose_campus_san_jose_occidental_mindoro),
                    R.drawable.credit_3
                ),
                Credits(getString(
                    R.string.leiza_linda_l_pelayo_maite),
                    getString(R.string.system_adviser_faculty_it_department_san_jose_campus_san_jose_occidental_mindoro),
                    R.drawable.credit_4
                )
            )
        )
    }

    private fun initAdapters() {
        binding.rvDev.layoutManager = LinearLayoutManager(this)
        developersAdapter = DevelopersAdapter(developers)
        binding.rvDev.adapter = developersAdapter

        binding.rvCredits.layoutManager = LinearLayoutManager(this)
        creditsAdapter = CreditsAdapter(credits)
        binding.rvCredits.adapter = creditsAdapter
    }
}