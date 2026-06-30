package com.bistu.focuslist.network

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NetworkApiTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun quoteApiParsesDailyQuoteResponse() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "hitokoto": "读书破万卷，下笔如有神。",
                      "from": "奉赠韦左丞丈二十二韵",
                      "from_who": "杜甫"
                    }
                    """.trimIndent()
                )
        )

        val api = RetrofitClient.createQuoteApi(server.url("/").toString())
        val response = api.getQuote()
        val request = server.takeRequest()

        assertEquals("/", request.path?.substringBefore("?"))
        assertEquals("读书破万卷，下笔如有神。", response.text)
        assertEquals("奉赠韦左丞丈二十二韵", response.from)
        assertEquals("杜甫", response.fromWho)
    }

    @Test
    fun templateApiParsesOnlineTemplatesResponse() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "templates": [
                        {
                          "id": "exam",
                          "title": "考试复习",
                          "category": "学习",
                          "description": "按章节复习并完成模拟题。",
                          "estimatedDays": 7,
                          "source": "在线模板",
                          "tasks": [
                            {
                              "title": "整理重点",
                              "notes": "列出老师强调的知识点。",
                              "category": "学习",
                              "priority": 3
                            }
                          ]
                        }
                      ]
                    }
                    """.trimIndent()
                )
        )

        val api = TemplateClient.createTemplateApi(server.url("/").toString())
        val response = api.getTemplates()
        val request = server.takeRequest()

        assertEquals("/focuslist/templates.json", request.path)
        assertEquals(1, response.templates.size)
        assertEquals("考试复习", response.templates.first().title)
        assertTrue(response.templates.first().tasks.isNotEmpty())
        assertEquals("整理重点", response.templates.first().tasks.first().title)
    }
}
