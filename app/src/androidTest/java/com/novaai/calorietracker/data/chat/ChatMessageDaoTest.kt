package com.novaai.calorietracker.data.chat

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatMessageDaoTest {

    private lateinit var db: NovaChatDatabase
    private lateinit var dao: ChatMessageDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, NovaChatDatabase::class.java).build()
        dao = db.chatMessageDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun savedMessageIsReadBackWithSameContent() = runTest {
        val id = dao.insert(ChatMessageEntity(text = "Hello Nova", sender = ChatSender.USER, timestamp = 1_000L))
        assertTrue(id > 0)

        val all = dao.getAll()
        assertEquals(1, all.size)
        assertEquals("Hello Nova", all[0].text)
        assertEquals(ChatSender.USER, all[0].sender)
        assertEquals(1_000L, all[0].timestamp)
    }

    @Test
    fun eachSavedMessageGetsAUniqueId() = runTest {
        val first = dao.insert(ChatMessageEntity(text = "Hi", sender = ChatSender.USER, timestamp = 1L))
        val second = dao.insert(ChatMessageEntity(text = "Hi", sender = ChatSender.USER, timestamp = 1L))
        assertNotEquals(first, second)
    }

    @Test
    fun messagesAreReturnedInChronologicalOrder() = runTest {
        dao.insert(ChatMessageEntity(text = "third", sender = ChatSender.NOVA, timestamp = 3_000L))
        dao.insert(ChatMessageEntity(text = "first", sender = ChatSender.USER, timestamp = 1_000L))
        dao.insert(ChatMessageEntity(text = "second", sender = ChatSender.NOVA, timestamp = 2_000L))

        val texts = dao.getAll().map { it.text }
        assertEquals(listOf("first", "second", "third"), texts)
    }

    @Test
    fun equalTimestampsFallBackToInsertionOrder() = runTest {
        dao.insert(ChatMessageEntity(text = "a", sender = ChatSender.USER, timestamp = 5_000L))
        dao.insert(ChatMessageEntity(text = "b", sender = ChatSender.NOVA, timestamp = 5_000L))

        val texts = dao.getAll().map { it.text }
        assertEquals(listOf("a", "b"), texts)
    }

    @Test
    fun clearAllRemovesEveryMessage() = runTest {
        dao.insert(ChatMessageEntity(text = "one", sender = ChatSender.USER, timestamp = 1L))
        dao.insert(ChatMessageEntity(text = "two", sender = ChatSender.NOVA, timestamp = 2L))

        dao.clearAll()

        assertTrue(dao.getAll().isEmpty())
    }

    @Test
    fun senderTypeSurvivesRoundTrip() = runTest {
        dao.insert(ChatMessageEntity(text = "from user", sender = ChatSender.USER, timestamp = 1L))
        dao.insert(ChatMessageEntity(text = "from nova", sender = ChatSender.NOVA, timestamp = 2L))

        val senders = dao.getAll().map { it.sender }
        assertEquals(listOf(ChatSender.USER, ChatSender.NOVA), senders)
    }
}
