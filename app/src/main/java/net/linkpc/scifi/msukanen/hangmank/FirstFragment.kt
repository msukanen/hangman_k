package net.linkpc.scifi.msukanen.hangmank

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import net.linkpc.scifi.msukanen.hangmank.databinding.FragmentFirstBinding
import kotlin.random.Random

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var wrongGuesses = 0
    private var rightGuessesRequired = 0
    private var rightGuesses = 0
    private lateinit var guessedChs: String
    private lateinit var wordToGuess: String
    private var gameOver: GameState = GameState.Running
    private val keys: MutableMap<Char, Button> = mutableMapOf()

    private val gameOn: Boolean get() = gameOver == GameState.Running

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        val alphabet = getString(R.string.alphabet)
        val buttons: List<Button> = listOf(
            binding.include.buttonA,
            binding.include.buttonB,
            binding.include.buttonC,
            binding.include.buttonD,
            binding.include.buttonE,
            binding.include.buttonF,
            binding.include.buttonG,
            binding.include.buttonH,
            binding.include.buttonI,
            binding.include.buttonJ,
            binding.include.buttonK,
            binding.include.buttonL,
            binding.include.buttonM,
            binding.include.buttonN,
            binding.include.buttonO,
            binding.include.buttonP,
            binding.include.buttonQ,
            binding.include.buttonR,
            binding.include.buttonS,
            binding.include.buttonT,
            binding.include.buttonU,
            binding.include.buttonV,
            binding.include.buttonW,
            binding.include.buttonX,
            binding.include.buttonY,
            binding.include.buttonZ
        )
        var i = 0
        alphabet.forEach { ch ->
            // lets bind a listener for each and every key in the "keyboard"...
            buttons[i].setOnClickListener { guess(ch) }
            keys[ch] = buttons[i]
            i += 1
        }

        binding.newGameButton.visibility = View.GONE
        binding.newGameButton.setOnClickListener {
            it.visibility = View.GONE

            // reset the playing field
            gameOver = GameState.Running
            wrongGuesses = 0
            rightGuesses = 0
            wordToGuess = fetchRandomWord()
            rightGuessesRequired = wordToGuess.toCharArray().distinct().size
            guessedChs = ""

            // (re-)enable all keys
            keys.forEach { k -> k.value.visibility = View.VISIBLE }

            updateHiddenWord()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //binding.buttonFirst.setOnClickListener {
        //    findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        //}

        savedInstanceState.also {
            wrongGuesses = it?.getInt("wrongGuesses") ?: 0
            rightGuesses = it?.getInt("rightGuesses") ?: 0
            wordToGuess = it?.getString("wordToGuess") ?: fetchRandomWord()
            rightGuessesRequired = wordToGuess.toCharArray().distinct().size
            guessedChs = it?.getString("guessedChs") ?: ""
            gameOver = it?.getEnum("gameOver", GameState.Running) ?: GameState.Running
        }

        updateHiddenWord()
        disableKeys(selectively = true)

        if (!gameOn) {
            disableKeys()
            binding.newGameButton.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun changeHangedManState() {
        binding.imageHangedMan.setBackgroundResource(
            when (wrongGuesses) {
                0 -> R.drawable.ic_hangman_1
                1 -> R.drawable.ic_hangman_1
                2 -> R.drawable.ic_hangman_2
                3 -> R.drawable.ic_hangman_3
                4 -> R.drawable.ic_hangman_4
                5 -> R.drawable.ic_hangman_5
                6 -> R.drawable.ic_hangman_6
                7 -> R.drawable.ic_hangman_7
                8 -> R.drawable.ic_hangman_8
                9 -> R.drawable.ic_hangman_9
                else -> R.drawable.ic_hangman_10
            }
        )
    }

    /**
     * Time to guess a letter...
     */
    private fun guess(ch: Char) {
        keys[ch]?.visibility = View.INVISIBLE
        guessedChs = guessedChs.plus(ch)

        if (!wordToGuess.contains(ch, ignoreCase = true)) {
            wrongGuesses += 1
            if (wrongGuesses >= MAX_GUESSES) {
                endGame(GameState.Loss)
                gameOver = GameState.Loss
                disableKeys()
                binding.newGameButton.visibility = View.VISIBLE
            }
        } else rightGuesses += 1

        if (gameOn && rightGuesses >= rightGuessesRequired)
            endGame(GameState.Victory)

        updateHiddenWord()
    }

    private fun endGame(state: GameState) {
        gameOver = state
        disableKeys()
        binding.newGameButton.visibility = View.VISIBLE
    }

    /**
     * Disable keys, either selectively or all of them.
     */
    private fun disableKeys(selectively: Boolean = false) {
        if (selectively) {
            // blank only those keys which have already been used
            guessedChs.forEach {
                keys[it]?.visibility = View.INVISIBLE
            }
        } else keys.forEach {
            it.value.visibility = View.INVISIBLE
        }
    }

    /**
     * Fetch a random word from internal "dictionary" of words.
     */
    private fun fetchRandomWord(): String {
        val words = resources.getStringArray(R.array.words)
        return words[Random.nextInt(0, words.size)]
    }

    /**
     * Update hidden word based on current game state.
     */
    private fun updateHiddenWord() {
        with(binding.textviewHiddenWord) {
            if (gameOver != GameState.Running)
                this.text = wordToGuess
            else this.text = wordToGuess.map { if (it in guessedChs) it else '-' }.joinToString("")
        }

        changeHangedManState()
    }

    companion object {
        const val MAX_GUESSES = 10
    }
}
