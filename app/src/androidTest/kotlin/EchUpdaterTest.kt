
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hippo.ehviewer.util.echUpdater
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EchUpdaterTest {

    @Test
    fun testEchUpdater() = runBlocking {
        echUpdater("exhentai.org")
    }
}
