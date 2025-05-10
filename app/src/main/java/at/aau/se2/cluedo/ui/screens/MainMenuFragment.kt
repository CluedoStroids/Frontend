package at.aau.se2.cluedo.ui.screens

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentMainMenuBinding

class MainMenuFragment : Fragment() {

    private var _binding: FragmentMainMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            binding.versionTextView.text = "v${packageInfo.versionName}"
        } catch (e: PackageManager.NameNotFoundException) {
            binding.versionTextView.text = "v1.0"
        }
        binding.createLobbyCard.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuFragment_to_lobbyFragment)
        }

        binding.joinLobbyCard.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuFragment_to_joinLobbyFragment)
        }



        binding.settingsCard.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuFragment_to_settingsFragment)
        }
        binding.gameboardCard.setOnClickListener {
            findNavController().navigate(R.id.action_mainMenuFragment_to_gameBoard)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
