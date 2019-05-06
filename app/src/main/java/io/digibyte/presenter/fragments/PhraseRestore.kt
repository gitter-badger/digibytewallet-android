package com.noahseidman.digiid

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import io.digibyte.R
import io.digibyte.databinding.FragmentPhraseRestoreBinding
import io.digibyte.presenter.adapter.MultiTypeDataBoundAdapter
import io.digibyte.presenter.fragments.NotificationFragment
import io.digibyte.presenter.fragments.interfaces.DialogCompleteCallback
import io.digibyte.presenter.fragments.interfaces.PhraseCallback
import io.digibyte.presenter.fragments.interfaces.PhraseCompleteCallback
import io.digibyte.presenter.fragments.models.LetterColumnViewModel
import io.digibyte.presenter.fragments.models.PhraseViewModel
import io.digibyte.tools.animation.BRDialog
import io.digibyte.tools.util.SeedUtil
import io.digibyte.wallet.BRWalletManager
import kotlinx.android.synthetic.main.fragment_phrase_restore.*

class PhraseRestore : NotificationFragment(), PhraseCallback, TextWatcher, View.OnClickListener {

    private lateinit var binding: FragmentPhraseRestoreBinding
    private val letters = arrayOf("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z")
    private lateinit var words: List<String>
    private var phraseCompleteCallback: PhraseCompleteCallback? = null

    @SuppressLint("SetTextI18n")
    override fun onClick(string: String?) {
        if (getSeedLength() < 24) {
            phrase.setText(phrase.text.toString() + " " + string)
        } else {
            processSeed(phrase.text.toString())
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.background -> fadeOutRemove()
            R.id.back -> removeWord()
        }
    }

    companion object {
        fun show(activity: AppCompatActivity, phraseCallback: PhraseCompleteCallback?, dialogCompleteCallback: DialogCompleteCallback) {
            val phraseRestore = PhraseRestore()
            phraseRestore.setPhraseCompleteCallback(phraseCallback)
            phraseRestore.setCompletion(dialogCompleteCallback)
            val transaction = activity.supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(
                    R.animator.from_bottom, R.animator.to_bottom,
                    R.animator.from_bottom, R.animator.to_bottom
            )
            transaction.add(android.R.id.content, phraseRestore, NotificationFragment::class.java.name)
            transaction.addToBackStack(NotificationFragment::class.java.name)
            transaction.commitAllowingStateLoss()
        }
    }

    fun setPhraseCompleteCallback(phraseCompleteCallback: PhraseCompleteCallback?) {
        this.phraseCompleteCallback = phraseCompleteCallback;
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPhraseRestoreBinding.inflate(inflater)
        context?.let {
            words = SeedUtil.getWordList(it)
            val letterColumns = ArrayList<Any>()
            var startIndex = 0
            for (i in 0 until letters.size) {
                val columnWords = ArrayList<Any>()
                for (p in startIndex until words.size) {
                    if (words.get(p).toLowerCase().startsWith(letters.get(i))) {
                        columnWords.add(PhraseViewModel(words.get(p)))
                    } else {
                        startIndex = p + 1
                        if (columnWords.size > 0) {
                            letterColumns.add(LetterColumnViewModel(MultiTypeDataBoundAdapter(this, columnWords)))
                        }
                        break
                    }
                }
            }
            binding.recycler.adapter = MultiTypeDataBoundAdapter(this, letterColumns)
            binding.phrase.addTextChangedListener(this)
            binding.back.setOnClickListener(this)
        }

        background = binding.background
        binding.background.setOnClickListener(this)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.recycler.scrollBy(100, 0)

    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    fun getSeedLength(): Int {
        return phrase.text.toString().trim().split(" ").size
    }

    fun removeWord() {
        phrase.setText(phrase.text.toString().trim().split(" ").subList(0, getSeedLength() - 1).joinToString(" "))
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        processSeed(s.toString())
    }

    fun processSeed(s: String) {
        if (BRWalletManager.validateRecoveryPhrase(words.toTypedArray(), s.trim()) && getSeedLength() == 24) {
            phraseCompleteCallback?.phrase(s)
        } else if (getSeedLength() == 24) {
            context?.let {
                BRDialog.showCustomDialog(it, "",
                        getString(R.string.RecoverWallet_invalid),
                        getString(R.string.AccessibilityLabels_close), null,
                        { brDialogView -> brDialogView.dismissWithAnimation() }, null, null, 0)
            }
        }
    }
}