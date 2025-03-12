import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.Timer
import java.util.TimerTask

class Screen4ViewModel(private val state: SavedStateHandle) : ViewModel() {

    companion object {
        private const val KEY_PROGRESS = "KEY_PROGRESS"
        private const val KEY_IS_PLAYING = "KEY_IS_PLAYING"
    }

    private val _currentProgress = MutableLiveData(state.get(KEY_PROGRESS) ?: 0)
    val currentProgress: LiveData<Int> = _currentProgress

    private val _isPlaying = MutableLiveData(state.get(KEY_IS_PLAYING) ?: false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _isLiked = MutableLiveData(false)
    val isLiked: LiveData<Boolean> = _isLiked

    private var timer: Timer? = null
    private val totalTime = 180  // 3 phút (tổng thời gian)

    init {
        if (_isPlaying.value == true) startTimer()
    }

    fun setPlaying(play: Boolean) {
        _isPlaying.value = play
        state[KEY_IS_PLAYING] = play
        if (play) startTimer() else stopTimer()
    }

    fun updateProgress(progress: Int) {
        _currentProgress.value = progress
        state[KEY_PROGRESS] = progress
    }

    fun toggleLike() {
        _isLiked.value = !(_isLiked.value ?: false)
    }

    private fun startTimer() {
        stopTimer() // Dừng timer cũ trước khi khởi tạo mới
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val newProgress = (_currentProgress.value ?: 0) + 1
                if (newProgress >= totalTime) stopTimer()
                _currentProgress.postValue(newProgress)
            }
        }, 1000, 1000)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    override fun onCleared() {
        stopTimer()
        super.onCleared()
    }

    fun getTotalTime() = totalTime
}
