import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.Timer
import java.util.TimerTask

class Screen4ViewModel(private val state: SavedStateHandle) : ViewModel() {

    companion object {
        private const val KEY_PROGRESS = "KEY_PROGRESS"
    }

    private val _currentProgress = MutableLiveData(state.get(KEY_PROGRESS) ?: 0)
    val currentProgress: LiveData<Int> = _currentProgress

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _isLiked = MutableLiveData(false)
    val isLiked: LiveData<Boolean> = _isLiked

    private var timer: Timer? = null
    private val totalTime = 180  // Giới hạn thời gian (ví dụ: 3 phút)

    fun updateProgress(progress: Int) {
        _currentProgress.value = progress
        state[KEY_PROGRESS] = progress
    }

    fun setPlaying(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
        if (isPlaying) {
            startTimer()
        } else {
            stopTimer()
        }
    }

    fun toggleLike() {
        _isLiked.value = _isLiked.value != true
    }

    private fun startTimer() {
        if (timer == null) {
            timer = Timer()
            timer?.schedule(object : TimerTask() {
                override fun run() {
                    _currentProgress.postValue((_currentProgress.value ?: 0) + 1)
                    if (_currentProgress.value ?: 0 >= totalTime) {
                        stopTimer()
                    }
                }
            }, 0, 1000) // Chạy mỗi giây
        }

    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
